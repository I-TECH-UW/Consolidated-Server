package org.itech.fhir.datarequest.core.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.model.base.AuditableEntity;
import org.itech.fhir.datarequest.core.model.converter.DataRequestStatusConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class DataRequestAttempt extends AuditableEntity<Long> {

	public enum DataRequestStatus {
		GENERATED('G'), REQUESTED('R'), ACCEPTED('A'), SUCCEEDED('S'), FAILED('F'), INCOMPLETE('I');

		private char code;

		private DataRequestStatus(char code) {
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
	private FhirServer server;

	// persistence
	@Convert(converter = DataRequestStatusConverter.class)
	// validation
	@NotNull
	private DataRequestStatus dataRequestStatus;

	@ManyToOne
	@JoinColumn(nullable = false, updatable = false)
	private FhirResourceGroup fhirResourceGroup;

	DataRequestAttempt() {
	}

	public DataRequestAttempt(FhirServer server, FhirResourceGroup fhirResourceGroup) {
		startTime = Instant.now();
		dataRequestStatus = DataRequestStatus.GENERATED;
		this.server = server;
		this.fhirResourceGroup = fhirResourceGroup;
	}

}
