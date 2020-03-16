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

	public static final TimeUnit MAX_INTERVAL_UNITS = TimeUnit.MINUTES;
	public static final TimeUnit MIN_INTERVAL_UNITS = TimeUnit.MINUTES;
	public static final TimeUnit TIMEOUT_UNITS = TimeUnit.SECONDS;

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

	private Integer dataRequestAttemptTimeout;

	private Integer maxDataImportInterval;

	private Integer minDataImportInterval;

	public DataImportTask() {
	}

	public DataImportTask(FhirServer server, FhirResourceGroup fhirResourceGroup) {
		this.fhirResourceGroup = fhirResourceGroup;
		this.sourceServer = server;
	}

}
