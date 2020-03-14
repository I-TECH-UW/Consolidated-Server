package org.itech.fhir.dataimport.api.service;

import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;

public interface DataImportStatusService {

	void changeDataRequestAttemptStatus(Long dataImportAttemptId, DataImportStatus requestStatus);

}
