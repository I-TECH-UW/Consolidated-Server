package org.itech.fhir.dataimport.api.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.dataimport.api.service.DataImportService;
import org.itech.fhir.dataimport.api.service.DataImportTaskCheckerService;
import org.itech.fhir.dataimport.core.dao.DataImportAttemptDAO;
import org.itech.fhir.dataimport.core.model.DataImportAttempt;
import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataImportTaskCheckerServiceImpl implements DataImportTaskCheckerService {

	private ServerService serverService;
	private DataImportTaskService dataImportTaskService;
	private DataImportAttemptDAO dataImportAttemptDAO;
	private DataImportService dataImportService;

	public DataImportTaskCheckerServiceImpl(ServerService serverService, DataImportTaskService dataImportTaskService,
			DataImportAttemptDAO dataImportAttemptDAO, DataImportService dataImportService) {
		log.info(this.getClass().getName() + " has started");
		this.serverService = serverService;
		this.dataImportTaskService = dataImportTaskService;
		this.dataImportAttemptDAO = dataImportAttemptDAO;
		this.dataImportService = dataImportService;
	}

	@Override
	@Scheduled(initialDelay = 10 * 1000, fixedRate = 60 * 1000)
	@Transactional
	public void checkDataRequestNeedsRunning() {
		log.debug("checking if servers need data import to be done");

		Instant now = Instant.now();

		Iterable<FhirServer> servers = serverService.getDAO().findAll();
		for (FhirServer server : servers) {
			Optional<DataImportTask> dataImportTaskOpt = dataImportTaskService.getDAO()
					.findDataImportTaskFromServer(server.getId());
			if (dataImportTaskOpt.isPresent()) {
				DataImportTask dataImportTask = dataImportTaskOpt.get();
				Instant nextDataRequestTime;
				List<DataImportAttempt> latestDataImportAttempts = dataImportAttemptDAO
						.findLatestDataImportAttemptsByDataImportTask(PageRequest.of(0, 1), dataImportTask.getId());
				if (!latestDataImportAttempts.isEmpty()) {
					DataImportAttempt lastDataImportAttempt = latestDataImportAttempts.get(0);
					nextDataRequestTime = lastDataImportAttempt.getStartTime()
							.plus(dataImportTask.getDataImportInterval(), DataImportTask.INTERVAL_UNITS.toChronoUnit());
				} else {
					nextDataRequestTime = now;
				}
				if (nextDataRequestTime.compareTo(now) <= 0) {
					log.debug("server found with dataImportTask needing to be run");
					try {
						dataImportService.importNewDataFromSourceToLocal(dataImportTask);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						log.error("error while importing data", e);
					}
				}
			}
		}
	}

}
