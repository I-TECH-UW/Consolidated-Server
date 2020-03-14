package org.itech.fhir.core.service.impl;

import java.net.URI;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.core.dao.ServerDAO;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ServerServiceImpl extends CrudServiceImpl<FhirServer, Long> implements ServerService {

	private ServerDAO serverRepository;

	public ServerServiceImpl(ServerDAO serverRepository) {
		super(serverRepository);
		log.info(this.getClass().getName() + " has started");
		this.serverRepository = serverRepository;
	}

	@Override
	public FhirServer saveNewServer(String name, String serverAddress) {
		log.debug("saving new server");
		FhirServer server = new FhirServer(name, serverAddress);
		FhirServer addedServer = serverRepository.save(server);
		return addedServer;
	}

	@Override
	public FhirServer saveNewServer(String name, URI dataRequestUrl) {
		log.debug("saving new server");
		FhirServer server = new FhirServer(name, dataRequestUrl);
		FhirServer addedServer = serverRepository.save(server);
		return addedServer;
	}

	@Override
	public void updateServerIdentifier(String oldName, String newName) {
		log.debug("updating server identifier");
		FhirServer server = serverRepository.findByName(oldName)
				.orElseThrow(() -> new ObjectNotFoundException(oldName, FhirServer.class.getName()));
		server.setName(newName);
	}

}
