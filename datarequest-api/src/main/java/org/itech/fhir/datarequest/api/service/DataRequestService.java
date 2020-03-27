package org.itech.fhir.datarequest.api.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;

import org.hl7.fhir.r4.model.Bundle;
import org.itech.fhir.datarequest.core.exception.DataRequestFailedException;

public interface DataRequestService {

	Future<List<Bundle>> requestDataFromServerSinceLastSucessfulRequest(Long serverId, Long fhirResourceGroupId)
			throws DataRequestFailedException;

	Future<List<Bundle>> requestDataFromServerFromInstant(Long serverId, Long fhirResourceGroupId, Instant lowerBound)
			throws DataRequestFailedException;

}
