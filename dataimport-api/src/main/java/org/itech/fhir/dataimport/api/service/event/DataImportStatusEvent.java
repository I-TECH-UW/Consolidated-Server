package org.itech.fhir.dataimport.api.service.event;

import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class DataImportStatusEvent extends ApplicationEvent {

	private static final long serialVersionUID = -9058748235265104418L;

	private Long dataImportAttemptId;
	private DataImportStatus dataImportStatus;

	public DataImportStatusEvent(Object source, Long dataImportAttemptId, DataImportStatus dataImportStatus) {
		super(source);
		this.dataImportAttemptId = dataImportAttemptId;
		this.dataImportStatus = dataImportStatus;
	}

}
