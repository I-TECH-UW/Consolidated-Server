package org.itech.fhir.dataimport.webapp.api.dto;

import java.net.URI;
import java.time.Instant;

import lombok.Data;

@Data
public class FhirServerDTO {

	private Long id;

	private URI uri;

	private String name;

	private Instant lastImportSuccess;

	private Instant lastImportAttempt;
}
