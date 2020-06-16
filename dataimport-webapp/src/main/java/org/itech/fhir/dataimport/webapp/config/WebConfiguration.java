package org.itech.fhir.dataimport.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	@Override
	// TODO remove the allow credentials once a proper authentication server can be
	// created
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("https://host.openelis.org:3000").allowedMethods("*")
				.allowCredentials(true);
	}

}
