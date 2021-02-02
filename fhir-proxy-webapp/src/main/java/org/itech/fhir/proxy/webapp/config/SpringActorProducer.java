package org.itech.fhir.proxy.webapp.config;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

public class SpringActorProducer implements IndirectActorProducer {

	private ApplicationContext applicationContext;
	private String beanName;
	private Object[] args;

	public SpringActorProducer(ApplicationContext applicationContext, String beanName, Object... args) {
		this.applicationContext = applicationContext;
		this.beanName = beanName;
		this.args = args;
	}

	@Override
	public Actor produce() {
		if (args == null) {
			return (Actor) applicationContext.getBean(beanName);
		} else {
			return (Actor) applicationContext.getBean(beanName, args);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Actor> actorClass() {
		return (Class<? extends Actor>) applicationContext.getType(beanName);
	}
}