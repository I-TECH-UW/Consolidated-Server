package org.itech.fhir.core.model;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
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

	private URI serverUrl;

	// persistence
	@Column(name = "name", unique = true, nullable = false, length = 255)
	// validation
	@NotBlank
	@Size(max = 255)
	@ValidName
	private String name;

	FhirServer() {
	}

	public FhirServer(String name, String serverAddress) {
		this.name = name;
		this.serverUrl = URIUtil.createHttpUrlFromString(serverAddress);
	}

	public FhirServer(String name, URI serverUrl) {
		this.name = name;
		this.serverUrl = serverUrl;
	}

}
