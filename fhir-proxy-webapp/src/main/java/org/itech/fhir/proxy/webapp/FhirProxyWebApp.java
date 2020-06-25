package org.itech.fhir.proxy.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableZuulProxy
@EnableJpaAuditing
@EnableJpaRepositories("org.itech")
@EntityScan("org.itech")
@ComponentScan("org.itech")
@PropertySource(value = { "classpath:application.properties" })
public class FhirProxyWebApp {

	public static void main(String[] args) {
		SpringApplication.run(FhirProxyWebApp.class, args);
	}

}
