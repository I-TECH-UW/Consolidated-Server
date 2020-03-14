package org.itech.fhir.datarequest.core.service;

import java.time.Instant;

import org.itech.fhir.core.service.CrudService;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;

public interface DataRequestAttemptService extends CrudService<DataRequestAttempt, Long> {

	Instant getLatestSuccessDateForSourceServerAndFhirResourceGroup(DataRequestAttempt dataRequestAttempt);

	Instant getLatestSuccessInstantForSourceServerAndFhirResourceGroup(Long serverId, Long fhirResourceGroupId);

}
