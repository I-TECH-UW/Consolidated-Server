package org.itech.fhir.datarequest.core.model;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.model.base.PersistenceEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class IncompleteDataRequest extends PersistenceEntity<Long> {

	// persistence
	@OneToOne
	@JoinColumn(nullable = false, updatable = false)
	// validation
	@NotNull
	private DataRequestAttempt dataRequestAttempt;

	@ElementCollection
	private Set<ResourceType> unfetchedResources;

}
