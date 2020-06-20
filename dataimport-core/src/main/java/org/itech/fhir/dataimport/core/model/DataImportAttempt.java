package org.itech.fhir.dataimport.core.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.base.PersistenceEntity;
import org.itech.fhir.dataimport.core.model.converter.DataImportStatusConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class DataImportAttempt extends PersistenceEntity<Long> {

	public enum DataImportStatus {
		GENERATED('G'), REQUESTED('R'), PERSISTING('P'), SUCCEEDED('S'), FAILED('F'), INCOMPLETE('I'), NOT_RAN('N');

		private char code;

		private DataImportStatus(char code) {
			this.code = code;
		}

		public char getCode() {
			return code;
		}
	}

	// persistence
	@Column(updatable = false)
	// validation
	@NotNull
	private Instant startTime;

	@Column
	private Instant endTime;

	// persistence
	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	// validation
	@NotNull
	private DataImportTask dataImportTask;

	// persistence
	@Convert(converter = DataImportStatusConverter.class)
	// validation
	@NotNull
	private DataImportStatus dataImportStatus;

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	private FhirResourceGroup fhirResourceGroup;

	DataImportAttempt() {
	}

	public DataImportAttempt(DataImportTask dataImportTask, FhirResourceGroup fhirResourceGroup) {
		startTime = Instant.now();
		dataImportStatus = DataImportStatus.GENERATED;
		this.dataImportTask = dataImportTask;
		this.fhirResourceGroup = fhirResourceGroup;
	}

}
