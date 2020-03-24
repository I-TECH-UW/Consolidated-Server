package org.itech.fhir.core.service;

import java.net.URI;

import org.itech.fhir.core.model.FhirServer;

public interface ServerService extends CrudService<FhirServer, Long> {

	FhirServer saveNewServer(String name, String serverUrl);

	FhirServer saveNewServer(String name, URI serverUrl);

	void updateServerIdentifier(String oldIdentifier, String newIdentifier);

	FhirServer getOrSaveNewServer(String name, URI serverUrl);

	FhirServer getOrSaveNewServer(URI serverUrl);

}
