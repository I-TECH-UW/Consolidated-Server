package org.itech.fhir.dataimport.api.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.itech.fhir.dataimport.core.model.DataImportTask;

public interface DataImportService {

	Future<DataImportStatus> importNewDataFromSourceToLocal(DataImportTask dataImportTask)
			throws InterruptedException, ExecutionException, TimeoutException;

	Future<DataImportStatus> importNewDataFromSourceToLocal(Long dataImportTaskId)
			throws InterruptedException, ExecutionException, TimeoutException;

	Future<DataImportStatus> importNewDataFromSourceToLocalForServer(Long serverId)
			throws InterruptedException, ExecutionException, TimeoutException;

}
