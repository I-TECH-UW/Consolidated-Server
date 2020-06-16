package org.itech.fhir.dataimport.webapp.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.dataimport.webapp.api.dto.CreateServerDTO;
import org.itech.fhir.dataimport.webapp.api.dto.FhirServerDTO;
import org.itech.fhir.dataimport.webapp.api.transform.FhirServerTransformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class ServerController {

	private ServerService serverService;
	private FhirServerTransformService fhirServerTransformService;

	public ServerController(ServerService serverService, FhirServerTransformService fhirServerTransformService) {
		this.serverService = serverService;
		this.fhirServerTransformService = fhirServerTransformService;
	}


	@GetMapping
	public ResponseEntity<Iterable<FhirServerDTO>> getServers() {
		return ResponseEntity.ok(fhirServerTransformService.getAsDTO(serverService.getDAO().findAll()));
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<FhirServerDTO> getServer(@PathVariable(value = "id") Long id) {
		FhirServer server = serverService.getDAO().findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(id, FhirServer.class.getName()));
		return ResponseEntity.ok(fhirServerTransformService.getAsDTO(server));
	}

	@GetMapping(value = "/name/{name}")
	public ResponseEntity<FhirServerDTO> getServerByName(@PathVariable(value = "name") String name) {
		FhirServer server = serverService.getDAO().findByName(name)
				.orElseThrow(() -> new ObjectNotFoundException(name, FhirServer.class.getName()));
		return ResponseEntity.ok(fhirServerTransformService.getAsDTO(server));
	}

	@GetMapping(value = "/name/{name}/available")
	public ResponseEntity<Map<String, String>> checkServerNameInUse(@PathVariable(value = "name") String name) {
		Optional<FhirServer> server = serverService.getDAO().findByName(name);
		if (server.isEmpty()) {
			return ResponseEntity.ok(Collections.singletonMap("response", "available"));
		} else {
			return ResponseEntity.ok(Collections.singletonMap("response", "in use"));
		}
	}

	@GetMapping(value = "{id}/name/{name}/available")
	public ResponseEntity<Map<String, String>> checkServerNameInUseByOther(@PathVariable(value = "id") Long id,
			@PathVariable(value = "name") String name) {
		Optional<FhirServer> server = serverService.getDAO().findByName(name);
		if (server.isEmpty()) {
			return ResponseEntity.ok(Collections.singletonMap("response", "available"));
		} else if (server.get().getId().equals(id)) {
			return ResponseEntity.ok(Collections.singletonMap("response", "in use by the provided id"));
		} else {
			return ResponseEntity.ok(Collections.singletonMap("response", "in use"));
		}
	}

	@PostMapping
	public ResponseEntity<FhirServerDTO> createServer(@RequestBody @Valid CreateServerDTO dto) {
		FhirServer newServer = serverService.saveNewServer(dto.getServerName(), dto.getServerUri());
		return ResponseEntity.ok(fhirServerTransformService.getAsDTO(newServer));
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<String> updateServer(@PathVariable(value = "id") Long id,
			@RequestBody @Valid CreateServerDTO dto) {
		serverService.updateServer(id, dto.getServerName(), dto.getServerUri());
		return ResponseEntity.ok("updated");
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteServer(@PathVariable(value = "id") Long id) {
		serverService.getDAO().deleteById(id);
		return ResponseEntity.ok("deleted");
	}

}
