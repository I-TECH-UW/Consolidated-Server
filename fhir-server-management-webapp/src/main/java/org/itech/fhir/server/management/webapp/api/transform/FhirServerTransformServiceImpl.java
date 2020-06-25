package org.itech.fhir.server.management.webapp.api.transform;

import java.util.ArrayList;
import java.util.List;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.server.management.webapp.api.dto.FhirServerDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FhirServerTransformServiceImpl implements FhirServerTransformService {


	public FhirServerTransformServiceImpl() {
	}

	@Override
	public Iterable<FhirServerDTO> getAsDTO(Iterable<FhirServer> fhirServers) {
		List<FhirServerDTO> fhirServerDTOs = new ArrayList<>();
		for (FhirServer fhirServer : fhirServers) {
			fhirServerDTOs.add(getAsDTO(fhirServer));
		}
		return fhirServerDTOs;
	}

	@Override
	@Transactional(readOnly = true)
	public FhirServerDTO getAsDTO(FhirServer fhirServer) {

		FhirServerDTO fhirServerDTO = new FhirServerDTO();
		fhirServerDTO.setId(fhirServer.getId());
		fhirServerDTO.setName(fhirServer.getName());
		fhirServerDTO.setUri(fhirServer.getUri());
		fhirServerDTO.setCode(fhirServer.getCode());
		fhirServerDTO.setLastCheckedIn(fhirServer.getLastCheckedIn());
		fhirServerDTO.setRegistered(fhirServer.getRegistered());

		return fhirServerDTO;
	}

}
