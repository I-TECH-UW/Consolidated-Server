package org.itech.fhir.dataimport.webapp.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.itech.fhir.dataimport.api.service.DataImportService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dataRequest")
public class DataRequestController {

	private DataImportService dataImportService;

	public DataRequestController(DataImportService dataImportService) {
		this.dataImportService = dataImportService;
	}

	@PostMapping("/server/{serverId}")
	public void runManualDataRequestsForServer(@PathVariable Long serverId)
			throws InterruptedException, ExecutionException, TimeoutException {
		dataImportService.importNewDataFromSourceToLocalForServer(serverId);
	}

	@PostMapping("/dataImportTask/{dataImportTaskId}")
	public void runManualDataRequest(@PathVariable Long dataImportTaskId)
			throws InterruptedException, ExecutionException, TimeoutException {
		dataImportService.importNewDataFromSourceToLocal(dataImportTaskId);
	}

}
