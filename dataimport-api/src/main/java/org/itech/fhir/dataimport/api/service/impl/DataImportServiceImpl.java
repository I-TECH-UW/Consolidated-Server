package org.itech.fhir.dataimport.api.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.hl7.fhir.r4.model.Bundle;
import org.itech.fhir.dataimport.api.service.DataImportService;
import org.itech.fhir.dataimport.api.service.DataImportStatusService;
import org.itech.fhir.dataimport.api.service.DataSaveService;
import org.itech.fhir.dataimport.core.dao.DataImportAttemptDAO;
import org.itech.fhir.dataimport.core.model.DataImportAttempt;
import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.itech.fhir.datarequest.api.service.DataRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DataImportServiceImpl implements DataImportService {

	private ConcurrentMap<Long, Object> serverIdLocks = new ConcurrentHashMap<>();

	@Value("${org.itech.dataimport.interval.default.min}")
	private Integer dataImportIntervalDefaultMinimum;
	@Value("${org.itech.dataimport.request.timeout.default}")
	private Integer defaultDataImportRequestTimeout;

	private DataRequestService dataRequestService;
	private DataSaveService dataSaveService;
	private DataImportTaskService dataImportTaskService;
	private DataImportAttemptDAO dataImportAttemptDAO;
	private DataImportStatusService dataImportStatusService;

	public DataImportServiceImpl(DataRequestService dataRequestService, DataSaveService dataForwardService,
			DataImportTaskService dataImportTaskService, DataImportAttemptDAO dataImportAttemptDAO,
			DataImportStatusService dataImportStatusService) {
		this.dataRequestService = dataRequestService;
		this.dataSaveService = dataForwardService;
		this.dataImportTaskService = dataImportTaskService;
		this.dataImportAttemptDAO = dataImportAttemptDAO;
		this.dataImportStatusService = dataImportStatusService;
	}

	@Override
	public Future<DataImportStatus> importNewDataFromSourceToLocalForServer(Long serverId)
			throws InterruptedException, ExecutionException, TimeoutException {
		return importNewDataFromSourceToLocal(
				dataImportTaskService.getDAO().findDataImportTaskFromServer(serverId).get());
	}

	@Override
	public Future<DataImportStatus> importNewDataFromSourceToLocal(Long dataImportTaskId) throws TimeoutException {
		return importNewDataFromSourceToLocal(dataImportTaskService.getDAO().findById(dataImportTaskId).get());
	}

	@Override
	@Async
	// TODO maybe attempt to wait a few seconds before attempting to grab data, as
	// data is often entered in batches rapidly, so we should get data after the
	// last attempt, not the first
	public Future<DataImportStatus> importNewDataFromSourceToLocal(DataImportTask dataImportTask) {
		synchronized (getLockForSourceServer(dataImportTask.getSourceServer().getId())) {
			if (!minimumTimeHasPassed(dataImportTask)) {
				log.debug("data import not run as it is was attempted too soon after the latest data import attempt");
				return new AsyncResult<>(DataImportStatus.NOT_RAN);
			}
			DataImportAttempt dataImportAttempt = dataImportAttemptDAO
					.save(new DataImportAttempt(dataImportTask, dataImportTask.getFhirResourceGroup()));
			return runDataImportAttempt(dataImportAttempt);

		}
	}

	private Future<DataImportStatus> runDataImportAttempt(DataImportAttempt dataImportAttempt) {
		DataImportTask dataImportTask = dataImportAttempt.getDataImportTask();
		try {
			dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(),
					DataImportStatus.REQUESTED);

			List<Bundle> searchResultBundles;
			Future<List<Bundle>> futureBundles = dataRequestService.requestDataFromServerFromInstant(
					dataImportTask.getSourceServer().getId(), dataImportTask.getFhirResourceGroup().getId(),
					dataImportTaskService.getLatestSuccessInstantForDataImportTask(dataImportTask));
			try {
				Integer timeout = dataImportTask.getDataRequestAttemptTimeout() == null
						? defaultDataImportRequestTimeout
						: dataImportTask.getDataRequestAttemptTimeout();
				searchResultBundles = futureBundles.get(timeout, DataImportTask.TIMEOUT_UNITS);
			} catch (TimeoutException e) {
				futureBundles.cancel(true);
				dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(),
						DataImportStatus.TIMED_OUT);
				return new AsyncResult<>(DataImportStatus.TIMED_OUT);
			}

			dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(), DataImportStatus.SAVING);
			dataSaveService.saveResultsFromServer(dataImportTask.getSourceServer().getId(), searchResultBundles);
			dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(),
					DataImportStatus.COMPLETE);
			return new AsyncResult<>(DataImportStatus.COMPLETE);
		} catch (ExecutionException | InterruptedException | RuntimeException e) {
			log.error("error occured while saving results to server", e);
			dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(), DataImportStatus.FAILED);
			return new AsyncResult<>(DataImportStatus.FAILED);
		}
	}

	// possibly move this into a service for locking capabilities if we need this
	// locking functionality to be global in the program
	// locks are not removed as this could introduce race conditions and there
	// should never be more than a few hundred locks in memory
	private Object getLockForSourceServer(Long serverId) {
		serverIdLocks.putIfAbsent(serverId, serverId);
		return serverIdLocks.get(serverId);
	}

	private boolean minimumTimeHasPassed(DataImportTask dataImportTask) {
		Instant now = Instant.now();
		Instant lastAttemptInstant = dataImportTaskService.getLatestInstantForDataImportTask(dataImportTask);

		Integer minimumIntervalTime = dataImportTask.getMinDataImportInterval() == null
				? dataImportIntervalDefaultMinimum
				: dataImportTask.getMinDataImportInterval();
		Instant minumumNextRuntime = lastAttemptInstant.plus(minimumIntervalTime,
				DataImportTask.MIN_INTERVAL_UNITS.toChronoUnit());
		return now.isAfter(minumumNextRuntime);
	}

}
