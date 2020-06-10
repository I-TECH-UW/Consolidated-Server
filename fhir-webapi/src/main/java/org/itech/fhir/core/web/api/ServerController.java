package org.itech.fhir.core.web.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.core.web.dto.CreateServerDTO;
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

	public ServerController(ServerService serverService) {
		this.serverService = serverService;
	}


	@GetMapping
	public ResponseEntity<Iterable<FhirServer>> getServers() {
		return ResponseEntity.ok(serverService.getDAO().findAll());
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<FhirServer> getServer(@PathVariable(value = "id") Long id) {
		FhirServer server = serverService.getDAO().findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(id, FhirServer.class.getName()));
		return ResponseEntity.ok(server);
	}

	@GetMapping(value = "/name/{name}")
	public ResponseEntity<FhirServer> getServerByName(@PathVariable(value = "name") String name) {
		FhirServer server = serverService.getDAO().findByName(name)
				.orElseThrow(() -> new ObjectNotFoundException(name, FhirServer.class.getName()));
		return ResponseEntity.ok(server);
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
	public ResponseEntity<FhirServer> createServer(@RequestBody @Valid CreateServerDTO dto) {
		FhirServer newServer = serverService.saveNewServer(dto.getServerName(), dto.getServerUri());
		return ResponseEntity.ok(newServer);
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
