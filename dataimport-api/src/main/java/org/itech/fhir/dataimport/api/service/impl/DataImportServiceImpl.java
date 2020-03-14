package org.itech.fhir.dataimport.api.service.impl;

import java.util.List;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DataImportServiceImpl implements DataImportService {

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
	// TODO synchronize on the server object if we can guarantee it will be the same
	public synchronized Future<DataImportStatus> importNewDataFromSourceToLocal(DataImportTask dataImportTask) {
		DataImportAttempt dataImportAttempt = dataImportAttemptDAO
				.save(new DataImportAttempt(dataImportTask, dataImportTask.getFhirResourceGroup()));
		try {
			dataImportStatusService.changeDataRequestAttemptStatus(dataImportAttempt.getId(),
					DataImportStatus.REQUESTED);

			List<Bundle> searchResultBundles;
			Future<List<Bundle>> futureBundles = dataRequestService.requestDataFromServerFromInstant(
					dataImportTask.getSourceServer().getId(), dataImportTask.getFhirResourceGroup().getId(),
					dataImportTaskService.getLatestSuccessInstantForDataImportTask(dataImportTask));
			try {
				searchResultBundles = futureBundles.get(dataImportTask.getDataRequestAttemptTimeout(),
						DataImportTask.TIMEOUT_UNITS);
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

}
