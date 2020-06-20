package org.itech.fhir.datarequest.core.service.impl;

import java.time.Instant;
import java.util.List;

import org.itech.fhir.core.service.impl.CrudServiceImpl;
import org.itech.fhir.datarequest.core.dao.DataRequestAttemptDAO;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;
import org.itech.fhir.datarequest.core.service.DataRequestAttemptService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DataRequestAttemptServiceImpl extends CrudServiceImpl<DataRequestAttempt, Long>
		implements DataRequestAttemptService {

	private DataRequestAttemptDAO dataRequestAttemptRepository;

	public DataRequestAttemptServiceImpl(DataRequestAttemptDAO dataRequestAttemptRepository) {
		super(dataRequestAttemptRepository);
		this.dataRequestAttemptRepository = dataRequestAttemptRepository;
	}

	@Override
	public Instant getLatestSuccessDateForSourceServerAndFhirResourceGroup(DataRequestAttempt dataRequestAttempt) {
		return getLatestSuccessInstantForSourceServerAndFhirResourceGroup(dataRequestAttempt.getServer().getId(),
				dataRequestAttempt.getFhirResourceGroup().getId());
	}

	@Override
	public Instant getLatestSuccessInstantForSourceServerAndFhirResourceGroup(Long serverId, Long fhirResourceGroupId) {
		Instant lastSuccess = Instant.EPOCH;

		List<DataRequestAttempt> lastRequestAttempts = dataRequestAttemptRepository
				.findLatestDataRequestAttemptsByServerAndFhirResourceGroupAndStatus(PageRequest.of(0, 1), serverId,
						fhirResourceGroupId, DataRequestStatus.SUCCEEDED);
		if (lastRequestAttempts.size() == 1) {
			DataRequestAttempt latestAttempt = lastRequestAttempts.get(0);
			lastSuccess = latestAttempt.getStartTime();
		}
		log.debug("last data request success was at: " + lastSuccess);
		return lastSuccess;
	}

}
