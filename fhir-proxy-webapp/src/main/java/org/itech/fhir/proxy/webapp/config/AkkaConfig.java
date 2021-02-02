package org.itech.fhir.proxy.webapp.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;

@Configuration
public class AkkaConfig {

	private ApplicationContext applicationContext;
	private AkkaSpringExtension extension;

	public AkkaConfig(ApplicationContext applicationContext, AkkaSpringExtension extension) {
		this.applicationContext = applicationContext;
		this.extension = extension;
	}

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem actorSystem = ActorSystem.create("OpenHIM-Actor", akkaConfiguration());
		extension.initialize(applicationContext);
		return actorSystem;
	}

	@Bean
	public Config akkaConfiguration() {
		return ConfigFactory.load();
	}

}
