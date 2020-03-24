package org.itech.fhir.core.service.impl;

import java.net.URI;
import java.util.Optional;

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
	public FhirServer saveNewServer(String name, String serverUrl) {
		log.debug("saving new server");
		FhirServer server = new FhirServer(name, serverUrl);
		FhirServer addedServer = serverRepository.save(server);
		return addedServer;
	}

	@Override
	public FhirServer saveNewServer(String name, URI serverUrl) {
		log.debug("saving new server");
		FhirServer server = new FhirServer(name, serverUrl);
		FhirServer addedServer = serverRepository.save(server);
		return addedServer;
	}

	@Override
	public FhirServer getOrSaveNewServer(String name, URI serverUrl) {
		Optional<FhirServer> fhirServer = serverRepository.findByAddress(serverUrl);
		if (fhirServer.isPresent()) {
			return fhirServer.get();
		} else {
			return saveNewServer(name, serverUrl);
		}
	}

	@Override
	public FhirServer getOrSaveNewServer(URI serverUrl) {
		Optional<FhirServer> fhirServer = serverRepository.findByAddress(serverUrl);
		if (fhirServer.isPresent()) {
			return fhirServer.get();
		} else {
			return saveNewServer(serverUrl.getHost(), serverUrl);
		}
	}

	@Override
	public void updateServerIdentifier(String oldName, String newName) {
		log.debug("updating server identifier");
		FhirServer server = serverRepository.findByName(oldName)
				.orElseThrow(() -> new ObjectNotFoundException(oldName, FhirServer.class.getName()));
		server.setName(newName);
	}

}
