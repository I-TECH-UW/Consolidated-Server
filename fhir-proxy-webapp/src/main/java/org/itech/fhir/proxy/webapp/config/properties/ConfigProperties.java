package org.itech.fhir.proxy.webapp.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "org.itech.fhir.proxy.webapp")
@Data
public class ConfigProperties {

	private String fhirStoreHost = "localhost";
	private Integer fhirStorePort = 8443;
	private String fhirStorePath = "/hapi-fhir-jpaserver/fhir/";

	private String openHIMCoreHost = "localhost";
	private String openHIMCoreUser = "root@openhim.org";
	private String openHIMCorePassword = "root";
	private Integer openHIMCorePort = 8080;

	private String serverHost = "localhost";
	private Integer serverPort = 9080;

}
