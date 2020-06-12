package org.itech.fhir.dataimport.webapp.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.datasubscriber.core.service.SubscriptionEventNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SubscriptionEventReceivingController {

	private SubscriptionEventNotificationService eventNotificationService;

	public SubscriptionEventReceivingController(SubscriptionEventNotificationService eventNotificationService) {
		this.eventNotificationService = eventNotificationService;
	}

	@RequestMapping("/subscription/event/**")
	public ResponseEntity<String> receiveEventForType(HttpServletRequest request,
			@RequestParam("sourceServerId") Long sourceServerId,
			@RequestParam("sourceResourcePath") URI resourcePath)
			throws ClientProtocolException, IOException, URISyntaxException {
		// fhir server is appending the resource type to the query string, not the path,
		// so we are retrieving it from the query string in this method
		log.debug("ping received from " + request.getRemoteHost() + ":" + request.getRemotePort());
		log.debug("ping received from server reporting as id " + sourceServerId);
		if (resourcePath != null) {
			log.debug("reporting resource path " + resourcePath);
			eventNotificationService.notifyForResourceFromSource(resourcePath, sourceServerId);
		} else {
			log.debug("no resource path found");
			eventNotificationService.notifyFromSource(sourceServerId);
		}

		return ResponseEntity.ok("");
	}

	@RequestMapping("/subscription/event/{resourceType}/**")
	public ResponseEntity<String> receiveEventForTypeForResource(
			@PathVariable("resourceType") ResourceType resourceType,
			@RequestParam("sourceServerId") Long sourceServerId)
			throws ClientProtocolException, IOException, URISyntaxException {
		log.debug("ping received for " + resourceType + " from server repporting as " + sourceServerId);
		eventNotificationService.notifyForResourceTypeFromSource(resourceType, sourceServerId);
		return ResponseEntity.ok("");
	}

}