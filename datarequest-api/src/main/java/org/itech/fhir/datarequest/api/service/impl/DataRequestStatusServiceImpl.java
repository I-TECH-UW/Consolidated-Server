package org.itech.fhir.datarequest.api.service.impl;

import java.time.Instant;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.datarequest.api.service.DataRequestAttemptStatusService;
import org.itech.fhir.datarequest.api.service.event.DataRequestStatusEvent;
import org.itech.fhir.datarequest.api.service.queue.DataRequestAttemptWaitQueue;
import org.itech.fhir.datarequest.core.dao.DataRequestAttemptDAO;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataRequestStatusServiceImpl implements DataRequestAttemptStatusService {


	private DataRequestAttemptDAO dataRequestAttemptRepository;
	private DataRequestAttemptWaitQueue dataRequestAttemptWaitQueue;
	private ApplicationEventPublisher applicationEventPublisher;

	public DataRequestStatusServiceImpl(DataRequestAttemptDAO dataRequestAttemptRepository,
			DataRequestAttemptWaitQueue dataRequestAttemptWaitQueue,
			ApplicationEventPublisher applicationEventPublisher) {
		this.dataRequestAttemptRepository = dataRequestAttemptRepository;
		this.dataRequestAttemptWaitQueue = dataRequestAttemptWaitQueue;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Transactional
	@EventListener(DataRequestStatusEvent.class)
	public void onApplicationEvent(DataRequestStatusEvent event) {
		DataRequestAttempt dataRequestAttempt;
		if (dataRequestAttemptWaitQueue.contains(event.getDataRequestAttemptId())) {
			dataRequestAttempt = dataRequestAttemptWaitQueue.getDataRequestAttempt(event.getDataRequestAttemptId());
		} else {
			dataRequestAttempt = dataRequestAttemptRepository.findById(event.getDataRequestAttemptId())
					.orElseThrow(() -> new ObjectNotFoundException(event.getDataRequestAttemptId(),
							DataRequestAttempt.class.getName()));
		}
		synchronized (dataRequestAttempt) {
			switch (event.getDataRequestStatus()) {
			case GENERATED:
				break;
			case REQUESTED:
				dataRequestAttemptWaitQueue.addDataRequestAttempt(dataRequestAttempt);
				break;
			case ACCEPTED:
				break;
			case SUCCEEDED:
			case FAILED:
			case INCOMPLETE:
				dataRequestAttemptWaitQueue.removeDataRequestAttempt(event.getDataRequestAttemptId());
				dataRequestAttempt.setEndTime(Instant.now());
			}
			dataRequestAttempt.setDataRequestStatus(event.getDataRequestStatus());
			dataRequestAttemptRepository.save(dataRequestAttempt);
		}
	}

	@Override
	public void changeDataRequestAttemptStatus(Long dataRequestAttemptId, DataRequestStatus requestStatus) {
		publishDataRequestEvent(dataRequestAttemptId, requestStatus);
	}

	private void publishDataRequestEvent(Long dataRequestAttemptId, DataRequestStatus dataRequestStatus) {
		log.debug("publishing dataRequestEvent for dataRequestAttempt " + dataRequestAttemptId + " with status "
				+ dataRequestStatus);
		applicationEventPublisher.publishEvent(new DataRequestStatusEvent(this, dataRequestAttemptId, dataRequestStatus));
	}

}
