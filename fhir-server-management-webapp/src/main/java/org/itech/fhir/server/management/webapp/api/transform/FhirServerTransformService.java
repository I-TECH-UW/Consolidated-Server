package org.itech.fhir.server.management.webapp.api.transform;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.server.management.webapp.api.dto.FhirServerDTO;

public interface FhirServerTransformService {

	Iterable<FhirServerDTO> getAsDTO(Iterable<FhirServer> fhirServers);

	FhirServerDTO getAsDTO(FhirServer fhirServer);
}
