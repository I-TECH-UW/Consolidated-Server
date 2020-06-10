package org.itech.fhir.core.service;

import java.net.URI;

import org.itech.fhir.core.dao.ServerDAO;
import org.itech.fhir.core.model.FhirServer;

public interface ServerService extends CrudService<FhirServer, Long> {

	FhirServer saveNewServer(String name, String serverUri);

	FhirServer saveNewServer(String name, URI uri);

	void updateServerName(String oldName, String newName);

	void updateServer(Long id, String name, String serverUri);

	FhirServer getOrSaveNewServer(String name, URI uri);

	FhirServer getOrSaveNewServer(URI uri);

	@Override
	ServerDAO getDAO();

}
