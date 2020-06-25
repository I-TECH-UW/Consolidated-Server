package org.itech.fhir.server.management.webapp.api;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.server.management.webapp.api.dto.FhirServerDTO;
import org.itech.fhir.server.management.webapp.api.transform.FhirServerTransformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server")
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

}
