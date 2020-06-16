package org.itech.fhir.dataimport.core.service.impl;

import java.time.Instant;
import java.util.List;

import org.itech.fhir.core.dao.FhirResourceGroupDAO;
import org.itech.fhir.core.dao.ServerDAO;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.impl.CrudServiceImpl;
import org.itech.fhir.dataimport.core.dao.DataImportAttemptDAO;
import org.itech.fhir.dataimport.core.dao.DataImportTaskDAO;
import org.itech.fhir.dataimport.core.model.DataImportAttempt;
import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.itech.fhir.dataimport.core.service.DataImportTaskService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DataImportTaskServiceImpl extends CrudServiceImpl<DataImportTask, Long> implements DataImportTaskService {

	private DataImportTaskDAO dataImportTaskDAO;
	private DataImportAttemptDAO dataImportAttemptDAO;
	private ServerDAO serverDAO;
	private FhirResourceGroupDAO fhirResourceGroupDAO;

	public DataImportTaskServiceImpl(DataImportTaskDAO dataImportTaskDAO, DataImportAttemptDAO dataImportAttemptDAO,
			ServerDAO serverDAO, FhirResourceGroupDAO fhirResourceGroupDAO) {
		super(dataImportTaskDAO);
		this.dataImportTaskDAO = dataImportTaskDAO;
		this.dataImportAttemptDAO = dataImportAttemptDAO;
		this.serverDAO = serverDAO;
		this.fhirResourceGroupDAO = fhirResourceGroupDAO;
	}

	@Override
	public DataImportTaskDAO getDAO() {
		return dataImportTaskDAO;
	}

	@Override
	public DataImportTask saveTaskToServer(Long serverId, Long fhirResourceGrouId) {
		return saveTaskToServer(serverDAO.findById(serverId).get(),
				fhirResourceGroupDAO.findById(fhirResourceGrouId).get());
	}

	@Override
	public DataImportTask saveTaskToServer(FhirServer server, FhirResourceGroup fhirResourceGroup) {
		log.debug("saving dataRequest task of type " + fhirResourceGroup.toString() + " to server");
		DataImportTask dataImportTask = new DataImportTask(server, fhirResourceGroup);
		return dataImportTaskDAO.save(dataImportTask);
	}

	@Override
	public DataImportTask saveTaskToServer(Long id, Long fhirResourceGrouId, Integer maxInterval) {
		return saveTaskToServer(serverDAO.findById(id).get(), fhirResourceGroupDAO.findById(fhirResourceGrouId).get(),
				maxInterval);
	}

	@Override
	public DataImportTask saveTaskToServer(FhirServer server, FhirResourceGroup fhirResourceGroup,
			Integer maxInterval) {
		log.debug("saving dataRequest task of type " + fhirResourceGroup + " to server with dataRequest maxInterval "
				+ maxInterval);
		DataImportTask dataImportTask = new DataImportTask(server, fhirResourceGroup);
		dataImportTask.setMaxDataImportInterval(maxInterval);
		return dataImportTaskDAO.save(dataImportTask);
	}


	@Override
	public Instant getLatestSuccessInstantForDataImportTask(DataImportTask dataImportTask) {
		Instant lastSuccess = Instant.EPOCH;

		List<DataImportAttempt> lastImportAttempts = dataImportAttemptDAO
				.findLatestDataImportAttemptsByDataImportTaskAndStatus(PageRequest.of(0, 1), dataImportTask.getId(),
						DataImportStatus.COMPLETE);
		if (lastImportAttempts.size() == 1) {
			DataImportAttempt latestAttempt = lastImportAttempts.get(0);
			lastSuccess = latestAttempt.getStartTime();
		}
		log.debug("last data import success was at: " + lastSuccess);
		return lastSuccess;
	}

	@Override
	public Instant getLatestInstantForDataImportTask(DataImportTask dataImportTask) {
		Instant lastAttempt = Instant.EPOCH;

		List<DataImportAttempt> lastImportAttempts = dataImportAttemptDAO
				.findLatestDataImportAttemptsByDataImportTask(PageRequest.of(0, 1), dataImportTask.getId());
		if (lastImportAttempts.size() == 1) {
			DataImportAttempt latestAttempt = lastImportAttempts.get(0);
			lastAttempt = latestAttempt.getStartTime();
		}
		log.debug("last data import attempt was at: " + lastAttempt);
		return lastAttempt;
	}

}
