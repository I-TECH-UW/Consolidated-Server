package org.itech.fhir.core.model;

import java.net.URI;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.itech.fhir.core.model.base.AuditableEntity;
import org.itech.fhir.core.util.URIUtil;
import org.itech.fhir.core.validation.annotation.ValidName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class FhirServer extends AuditableEntity<Long> {

	private URI uri;

	// persistence
	@Column(name = "name", unique = true, length = 255)
	// validation
	@Size(max = 255)
	@ValidName
	private String name;

	// persistence
	@Column(name = "code", unique = true, length = 255)
	// validation
	@Size(max = 255)
	@ValidName
	private String code;

	// validation
	@NotNull
	private Instant registered;

	private Instant lastCheckedIn;

	FhirServer() {
	}

	public FhirServer(String name) {
		this.name = name;
		registered = Instant.now();
	}

	public FhirServer(String name, String uri) {
		this.name = name;
		this.uri = URIUtil.createHttpUrlFromString(uri);
		registered = Instant.now();
	}

	public FhirServer(String name, URI uri) {
		this.name = name;
		this.uri = uri;
		registered = Instant.now();
	}

	public void setUri(String uri) {
		this.uri = URIUtil.createHttpUrlFromString(uri);
	}

}
