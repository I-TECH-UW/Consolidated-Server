package org.itech.fhir.dataimport.core.model;

import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.model.base.AuditableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class DataImportTask extends AuditableEntity<Long> {

	public static final TimeUnit INTERVAL_UNITS = TimeUnit.MINUTES;
	public static final TimeUnit TIMEOUT_UNITS = TimeUnit.SECONDS;

	private static final Integer DEFAULT_INTERVAL = 60 * 24 * 7;
	private static final Integer DEFAULT_TIMEOUT = 60 * 2;

	// persistence
	@ManyToOne
	@JoinColumn(nullable = false)
	// validation
	@NotNull
	private FhirResourceGroup fhirResourceGroup;

	// persistence
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	// validation
	@NotNull
	private FhirServer sourceServer;

	// validation
	@NotNull
	private Integer dataRequestAttemptTimeout = DEFAULT_TIMEOUT;

	// validation
	@NotNull
	private Integer dataImportInterval = DEFAULT_INTERVAL;

	public DataImportTask() {
	}

	public DataImportTask(FhirServer server, FhirResourceGroup fhirResourceGroup) {
		this.fhirResourceGroup = fhirResourceGroup;
		this.sourceServer = server;
	}

}
