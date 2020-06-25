package org.itech.fhir.server.management.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	@Value("${org.itech.cors.allowedOrigins}")
	private String allowedOrigins;
	@Value("${org.itech.cors.allowedMethods:*}")
	private String allowedMethods;
	@Value("${org.itech.cors.allowCredentials:true}")
	private Boolean allowCredentials;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins(allowedOrigins).allowedMethods(allowedMethods)
				.allowCredentials(allowCredentials);
	}

}
