package org.itech.fhir.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.model.base.PersistenceEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "resource_type", "param_name", "fhir_resource_group_id" }) })
public class ResourceSearchParam extends PersistenceEntity<Long> {

	@Column(name = "resource_type", nullable = false, updatable = false)
	private ResourceType resourceType;

	@Column(name = "param_name")
	private String paramName;

	@ElementCollection
	private List<String> paramValues;

	@ManyToOne
	@JoinColumn(name = "fhir_resource_group_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private FhirResourceGroup fhirResourceGroup;

	ResourceSearchParam() {

	}

	public ResourceSearchParam(FhirResourceGroup fhirResourceGroup, ResourceType resourceType) {
		this.fhirResourceGroup = fhirResourceGroup;
		this.resourceType = resourceType;
		paramValues = new ArrayList<>();
	}


	public ResourceSearchParam(FhirResourceGroup fhirResourceGroup, ResourceType resourceType,
			String paramName, List<String> paramValues) {
		this.fhirResourceGroup = fhirResourceGroup;
		this.resourceType = resourceType;
		this.paramName = paramName;
		this.paramValues = paramValues;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (!(object instanceof ResourceSearchParam)) {
			return false;
		}
		ResourceSearchParam resourceSearchParam = (ResourceSearchParam) object;


		return Objects.equals(resourceSearchParam.resourceType, this.resourceType)
				&& Objects.equals(resourceSearchParam.paramName, this.paramName) //
				&& Objects.equals(resourceSearchParam.fhirResourceGroup, this.fhirResourceGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceType, paramName, fhirResourceGroup);
	}

}
