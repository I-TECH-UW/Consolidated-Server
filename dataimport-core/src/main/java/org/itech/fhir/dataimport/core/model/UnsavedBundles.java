package org.itech.fhir.dataimport.core.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hl7.fhir.r4.model.Bundle;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.model.base.PersistenceEntity;

import lombok.Data;

@Entity
@Data
public class UnsavedBundles extends PersistenceEntity<Long> {

	// persistence
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "source_server_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	// validation
	@NotNull
	private FhirServer sourceServer;

	@ElementCollection
	private List<Bundle> unsavedBundles;

}
