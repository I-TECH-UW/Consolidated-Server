package org.itech.fhir.dataimport.api.service.impl;

import java.time.Instant;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.dataimport.api.service.DataImportStatusService;
import org.itech.fhir.dataimport.api.service.event.DataImportStatusEvent;
import org.itech.fhir.dataimport.core.dao.DataImportAttemptDAO;
import org.itech.fhir.dataimport.core.model.DataImportAttempt;
import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataImportStatusServiceImpl implements DataImportStatusService {

	private DataImportAttemptDAO dataImportAttemptRepository;
	private ApplicationEventPublisher applicationEventPublisher;

	public DataImportStatusServiceImpl(DataImportAttemptDAO dataImportAttemptRepository,
			ApplicationEventPublisher applicationEventPublisher) {
		this.dataImportAttemptRepository = dataImportAttemptRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Transactional
	@EventListener(DataImportStatusEvent.class)
	public void onApplicationEvent(DataImportStatusEvent event) {
		DataImportAttempt dataImportAttempt = dataImportAttemptRepository.findById(event.getDataImportAttemptId())
				.orElseThrow(() -> new ObjectNotFoundException(event.getDataImportAttemptId(),
						DataRequestAttempt.class.getName()));
		switch (event.getDataImportStatus()) {
		case GENERATED:
			// never published
			break;
		case REQUESTED:
			break;
		case SAVING:
			break;
		case COMPLETE:
		case FAILED:
		case TIMED_OUT:
		case NOT_RAN:
			dataImportAttempt.setEndTime(Instant.now());
		}
		dataImportAttempt.setDataImportStatus(event.getDataImportStatus());
		dataImportAttemptRepository.save(dataImportAttempt);
	}

	@Override
	public void changeDataRequestAttemptStatus(Long dataImportAttemptId, DataImportStatus dataImportStatus) {
		publishDataRequestEvent(dataImportAttemptId, dataImportStatus);
	}

	private void publishDataRequestEvent(Long dataImportAttemptId, DataImportStatus dataImportStatus) {
		log.debug("publishing dataImportEvent for dataImportAttempt " + dataImportAttemptId + " with status "
				+ dataImportStatus);
		applicationEventPublisher.publishEvent(new DataImportStatusEvent(this, dataImportAttemptId, dataImportStatus));
	}

}
