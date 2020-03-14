package org.itech.fhir.core.service;

import java.net.URI;

import org.itech.fhir.core.model.FhirServer;

public interface ServerService extends CrudService<FhirServer, Long> {

	FhirServer saveNewServer(String name, String serverAddress);

	FhirServer saveNewServer(String name, URI dataRequestUrl);

	void updateServerIdentifier(String oldIdentifier, String newIdentifier);

}
