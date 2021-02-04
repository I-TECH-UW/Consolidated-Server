package org.itech.fhir.proxy.webapp.config;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.itech.fhir.proxy.webapp.config.properties.ConfigProperties;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.MediatorConfig.SSLContext;
import org.openhim.mediator.engine.MediatorServer;
import org.openhim.mediator.engine.RegistrationConfig;
import org.openhim.mediator.engine.RoutingTable;
import org.openhim.mediator.engine.RoutingTable.RouteAlreadyMappedException;
import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenHIMMediatorStarter {

	private ActorSystem actorSystem;
	private ConfigProperties configProperties;

	public OpenHIMMediatorStarter(ActorSystem actorSystem, ConfigProperties configProperties) {
		this.actorSystem = actorSystem;
		this.configProperties = configProperties;
	}

	@PostConstruct
	public void startMediator() throws IOException, RouteAlreadyMappedException {
		// setup actor system
		final ActorSystem system = actorSystem;
		// setup actors
		log.info("Initializing mediator actors...");

		MediatorConfig config = new MediatorConfig("cs-mediator", configProperties.getServerHost(),
				configProperties.getServerPort());
		config.setCoreHost(configProperties.getOpenHIMCoreHost());
		config.setCoreAPIUsername(configProperties.getOpenHIMCoreUser());
		config.setCoreAPIPassword(configProperties.getOpenHIMCorePassword());
		config.setCoreAPIPort(configProperties.getOpenHIMCorePort());
		config.setHeartbeatsEnabled(true);
		SSLContext sslContext = new SSLContext(true);
		config.setSSLContext(sslContext);

		RoutingTable routingTable = new RoutingTable();
		routingTable.addRegexRoute("/.*", OpenHIMMediatorActor.class);
		config.setRoutingTable(routingTable);

		InputStream regInfo = this.getClass().getClassLoader().getResourceAsStream("mediatorConfig.json");
		RegistrationConfig regConfig = new RegistrationConfig(regInfo);
		config.setRegistrationConfig(regConfig);

		final MediatorServer server = new MediatorServer(system, config);

		// setup shutdown hook (will handle ctrl-c)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Shutting down mediator");
				server.stop();
				system.shutdown();
			}
		});

		log.info("Starting mediator server...");
		server.start();

		log.info("Mediator listening on " + config.getServerHost() + ":" + config.getServerPort());

	}

}
