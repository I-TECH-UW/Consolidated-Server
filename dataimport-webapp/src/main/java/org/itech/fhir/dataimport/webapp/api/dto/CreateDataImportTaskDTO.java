package org.itech.fhir.dataimport.webapp.api.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateDataImportTaskDTO {

	@NotNull
	private Long fhirResourceGroupId;

	private Integer interval;

}
