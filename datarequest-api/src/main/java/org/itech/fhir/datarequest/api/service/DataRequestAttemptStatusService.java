package org.itech.fhir.datarequest.api.service;

import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;

public interface DataRequestAttemptStatusService {

	void changeDataRequestAttemptStatus(Long dataRequestAttemptId, DataRequestStatus requestStatus);

}
