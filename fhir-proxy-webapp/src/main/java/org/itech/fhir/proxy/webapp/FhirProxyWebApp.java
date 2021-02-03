package org.itech.fhir.proxy.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableZuulProxy
@EnableJpaAuditing
@EnableJpaRepositories("org.itech")
@EntityScan("org.itech")
@ComponentScan("org.itech")
@ConfigurationPropertiesScan("org.itech")
@PropertySources({ @PropertySource(value = { "classpath:application.properties" }),
		@PropertySource(value = "file:/run/secrets/extra.properties", ignoreResourceNotFound = true) })
public class FhirProxyWebApp {

	public static void main(String[] args) {
		SpringApplication.run(FhirProxyWebApp.class, args);
	}

}
