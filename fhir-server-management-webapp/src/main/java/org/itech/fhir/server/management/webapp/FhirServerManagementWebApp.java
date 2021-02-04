package org.itech.fhir.server.management.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories("org.itech")
@EntityScan("org.itech")
@ComponentScan("org.itech")
@PropertySources({ @PropertySource(value = { "classpath:application.properties" }),
		@PropertySource(value = "file:/run/secrets/extra.properties", ignoreResourceNotFound = true) })
public class FhirServerManagementWebApp {

	public static void main(String[] args) {
		SpringApplication.run(FhirServerManagementWebApp.class, args);
	}

}
