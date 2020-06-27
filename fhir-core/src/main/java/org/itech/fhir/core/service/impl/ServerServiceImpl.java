package org.itech.fhir.core.service.impl;

import java.net.URI;
import java.time.Instant;
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
	public FhirServer saveNewServer(String name, URI uri) {
		log.debug("saving new server");
		FhirServer server = new FhirServer(name, uri);
		FhirServer addedServer = serverRepository.save(server);
		return addedServer;
	}

	@Override
	public FhirServer getOrSaveNewServer(String name, URI uri) {
		Optional<FhirServer> fhirServer = serverRepository.findByAddress(uri);
		if (fhirServer.isPresent()) {
			return fhirServer.get();
		} else {
			return saveNewServer(name, uri);
		}
	}

	@Override
	public FhirServer getOrSaveNewServer(URI uri) {
		Optional<FhirServer> fhirServer = serverRepository.findByAddress(uri);
		if (fhirServer.isPresent()) {
			return fhirServer.get();
		} else {
			return saveNewServer(uri.getHost(), uri);
		}
	}

	@Override
	public void updateServer(Long id, String name, String uri) {
		log.debug("updating server");
		FhirServer server = serverRepository.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(id, FhirServer.class.getName()));
		server.setName(name);
		server.setUri(uri);
	}

	@Override
	public void updateServerName(String oldName, String newName) {
		log.debug("updating server name");
		FhirServer server = serverRepository.findByName(oldName)
				.orElseThrow(() -> new ObjectNotFoundException(oldName, FhirServer.class.getName()));
		server.setName(newName);
	}

	@Override
	public ServerDAO getDAO() {
		return serverRepository;
	}

	@Override
	public void receiveNotificationFromServer(String serverName, String serverCode) {
		Optional<FhirServer> server = serverRepository.findByCode(serverCode);
		if (!server.isPresent()) {
			server = Optional.of(serverRepository.save(new FhirServer(serverName)));
			server.get().setCode(serverCode);
		}
		updateServerLastReceivedTime(server.get(), Instant.now());
	}

	private void updateServerLastReceivedTime(FhirServer fhirServer, Instant lastReceivedTime) {
		fhirServer.setLastCheckedIn(lastReceivedTime);
		serverRepository.save(fhirServer);
	}

}
