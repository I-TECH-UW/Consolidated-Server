package org.itech.fhir.dataimport.core.service;

import java.time.Instant;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.CrudService;
import org.itech.fhir.dataimport.core.dao.DataImportTaskDAO;
import org.itech.fhir.dataimport.core.model.DataImportTask;

public interface DataImportTaskService extends CrudService<DataImportTask, Long> {

	@Override
	DataImportTaskDAO getDAO();

	DataImportTask saveTaskToServer(Long serverId, Long fhirResourceGroupId);

	DataImportTask saveTaskToServer(FhirServer server, FhirResourceGroup fhirResourceGroup);

	DataImportTask saveTaskToServer(Long serverId, Long fhirResourceGroupId, Integer interval);

	DataImportTask saveTaskToServer(FhirServer server, FhirResourceGroup fhirResourceGroup, Integer interval);

	Instant getLatestSuccessInstantForDataImportTask(DataImportTask dataImportTask);
}
