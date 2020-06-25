package org.itech.fhir.server.management.webapp.api.dto;

import java.net.URI;
import java.time.Instant;

import lombok.Data;

@Data
public class FhirServerDTO {

	private Long id;

	private URI uri;

	private String name;

	private String code;

	private Instant registered;

	private Instant lastCheckedIn;
}
