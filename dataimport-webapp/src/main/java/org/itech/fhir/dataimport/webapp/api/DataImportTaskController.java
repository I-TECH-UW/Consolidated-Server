package org.itech.fhir.dataimport.webapp.api;

import javax.validation.Valid;

import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.itech.fhir.dataimport.webapp.api.dto.CreateDataImportTaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dataImportTask/server/{serverId}")
public class DataImportTaskController {

	private DataImportTaskService dataImportTaskService;

	public DataImportTaskController(DataImportTaskService serverDataImportTaskService) {
		this.dataImportTaskService = serverDataImportTaskService;
	}

	@GetMapping
	public ResponseEntity<DataImportTask> getDataImportTaskForServer(
			@PathVariable(value = "serverId") Long serverId) {
		return ResponseEntity.ok(dataImportTaskService.getDAO().findDataImportTaskFromServer(serverId).get());
	}

	@PostMapping
	public ResponseEntity<DataImportTask> addDataImportTaskForServer(@PathVariable(value = "serverId") Long serverId,
			@RequestBody @Valid CreateDataImportTaskDTO dto) {
		DataImportTask newDataImportTask;
		if (dto.getInterval() == null) {
			newDataImportTask = dataImportTaskService.saveTaskToServer(serverId, dto.getFhirResourceGroupId());
		} else {
			newDataImportTask = dataImportTaskService.saveTaskToServer(serverId, dto.getFhirResourceGroupId(),
					dto.getInterval());
		}
		return ResponseEntity.ok(newDataImportTask);
	}

	@PutMapping
	public ResponseEntity<DataImportTask> updateDataImportTaskForServer(
			@PathVariable(value = "serverId") Long serverId, @RequestBody @Valid CreateDataImportTaskDTO dto) {
		DataImportTask newDataImportTask;
		if (dto.getInterval() == null) {
			newDataImportTask = dataImportTaskService.saveTaskToServer(serverId, dto.getFhirResourceGroupId());
		} else {
			newDataImportTask = dataImportTaskService.saveTaskToServer(serverId, dto.getFhirResourceGroupId(),
					dto.getInterval());
		}
		return ResponseEntity.ok(newDataImportTask);
	}
}
