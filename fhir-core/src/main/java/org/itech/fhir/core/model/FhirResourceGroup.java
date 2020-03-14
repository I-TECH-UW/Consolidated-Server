package org.itech.fhir.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.itech.fhir.core.model.base.AuditableEntity;
import org.itech.fhir.core.validation.annotation.ValidName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class FhirResourceGroup extends AuditableEntity<Long> {

	// persistence
	@Column(unique = true)
	// validation
	@NotBlank
	@Size(min = 1, max = 255)
	@ValidName
	private String resourceGroupName;

	FhirResourceGroup() {
	}

	public FhirResourceGroup(String resourceGroupName) {
		this.resourceGroupName = resourceGroupName;
	}


}
