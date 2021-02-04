package org.itech.fhir.proxy.webapp.config;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import akka.actor.Extension;
import akka.actor.Props;

@Component
public class AkkaSpringExtension implements Extension {

	private ApplicationContext applicationContext;

	public void initialize(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Props props(String actorBeanName) {
		return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
	}

	public Props props(String actorBeanName, Object... args) {
		return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
	}
}
