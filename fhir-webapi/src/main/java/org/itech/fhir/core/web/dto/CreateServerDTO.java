package org.itech.fhir.core.web.dto;

import javax.validation.constraints.NotBlank;

import org.itech.fhir.core.validation.annotation.ValidName;

import lombok.Data;

@Data
public class CreateServerDTO {

	@NotBlank
	@ValidName
	private String serverName;

	@NotBlank
	private String serverUri;

}
