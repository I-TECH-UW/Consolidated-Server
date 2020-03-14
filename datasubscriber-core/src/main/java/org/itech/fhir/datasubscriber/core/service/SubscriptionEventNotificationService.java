package org.itech.fhir.datasubscriber.core.service;

import java.net.URI;

import org.hl7.fhir.r4.model.ResourceType;

public interface SubscriptionEventNotificationService {

	void notifyForResourceFromSource(URI resourcePath, Long sourceServerId);

	void notifyForResourceTypeFromSource(ResourceType resourceType, Long sourceServerId);

	void notifyFromSource(Long sourceServerId);

}
