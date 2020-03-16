package org.itech.fhir.dataimport.api.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.dataimport.api.service.DataImportService;
import org.itech.fhir.dataimport.api.service.DataImportTaskCheckerService;
import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataImportTaskCheckerServiceImpl implements DataImportTaskCheckerService {

	@Value("${org.itech.dataimport.interval.default.max}")
	private Integer dataImportIntervalDefaultMaximum;

	private ServerService serverService;
	private DataImportTaskService dataImportTaskService;
	private DataImportService dataImportService;

	public DataImportTaskCheckerServiceImpl(ServerService serverService, DataImportTaskService dataImportTaskService,
			DataImportService dataImportService) {
		log.info(this.getClass().getName() + " has started");
		this.serverService = serverService;
		this.dataImportTaskService = dataImportTaskService;
		this.dataImportService = dataImportService;
	}

	@Override
	@Scheduled(initialDelay = 10 * 1000, fixedRate = 60 * 1000)
	@Transactional
	public void checkDataRequestNeedsRunning() {
		log.debug("checking if servers need data import to be done");

		Iterable<FhirServer> servers = serverService.getDAO().findAll();
		for (FhirServer server : servers) {
			Optional<DataImportTask> dataImportTaskOpt = dataImportTaskService.getDAO()
					.findDataImportTaskFromServer(server.getId());
			if (dataImportTaskOpt.isPresent()) {
				DataImportTask dataImportTask = dataImportTaskOpt.get();
				if (maximumTimeHasPassed(dataImportTask)) {
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

	private boolean maximumTimeHasPassed(DataImportTask dataImportTask) {
		Instant now = Instant.now();
		Instant lastAttemptInstant = dataImportTaskService.getLatestInstantForDataImportTask(dataImportTask);

		Integer maximumIntervalTime = dataImportTask.getMaxDataImportInterval() == null
				? dataImportIntervalDefaultMaximum
				: dataImportTask.getMaxDataImportInterval();
		Instant nextScheduledRuntime = lastAttemptInstant.plus(maximumIntervalTime,
				DataImportTask.MAX_INTERVAL_UNITS.toChronoUnit());
		return now.isAfter(nextScheduledRuntime);
	}

}
