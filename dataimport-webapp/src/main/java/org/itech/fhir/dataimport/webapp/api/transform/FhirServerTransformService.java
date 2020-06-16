package org.itech.fhir.dataimport.webapp.api.transform;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.dataimport.webapp.api.dto.FhirServerDTO;

public interface FhirServerTransformService {

	Iterable<FhirServerDTO> getAsDTO(Iterable<FhirServer> fhirServers);

	FhirServerDTO getAsDTO(FhirServer fhirServer);
}
