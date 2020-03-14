package org.itech.fhir.core.web.api;

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

	@PostMapping
	public ResponseEntity<FhirServer> createServer(@RequestBody @Valid CreateServerDTO dto) {
		FhirServer newServer = serverService.saveNewServer(dto.getName(),
				dto.getServerAddress());
		return ResponseEntity.ok(newServer);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<FhirServer> getServer(@PathVariable(value = "id") Long id) {
		FhirServer server = serverService.getDAO().findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(id, FhirServer.class.getName()));
		return ResponseEntity.ok(server);
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteServer(@PathVariable(value = "id") Long id) {
		serverService.getDAO().deleteById(id);
		return ResponseEntity.ok("deleted");
	}

}
