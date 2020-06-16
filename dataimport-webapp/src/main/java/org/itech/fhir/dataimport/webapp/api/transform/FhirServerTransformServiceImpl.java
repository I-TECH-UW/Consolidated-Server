package org.itech.fhir.dataimport.webapp.api.transform;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.itech.fhir.dataimport.webapp.api.dto.FhirServerDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FhirServerTransformServiceImpl implements FhirServerTransformService {

	DataImportTaskService dataImportTaskService;

	public FhirServerTransformServiceImpl(DataImportTaskService dataImportTaskService) {
		this.dataImportTaskService = dataImportTaskService;
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

		Optional<DataImportTask> dataImportTask = dataImportTaskService.getDAO()
				.findDataImportTaskFromServer(fhirServer.getId());
		if (dataImportTask.isPresent()) {
			Instant latestSuccess = dataImportTaskService
					.getLatestSuccessInstantForDataImportTask(dataImportTask.get());
			Instant latestAttempt = dataImportTaskService.getLatestInstantForDataImportTask(dataImportTask.get());
			fhirServerDTO.setLastImportSuccess(latestSuccess);
			fhirServerDTO.setLastImportAttempt(latestAttempt);
		}

		return fhirServerDTO;
	}

}
