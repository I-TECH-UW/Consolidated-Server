package org.itech.fhir.datasubscriber.core.service;

import java.io.UnsupportedEncodingException;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.CrudService;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;

public interface BulkSubscriptionService extends CrudService<BulkSubscription, Long> {


	BulkSubscription createBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId)
			throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(FhirServer sourceServer, FhirResourceGroup fhirResourceGroup)
			throws UnsupportedEncodingException;

}
