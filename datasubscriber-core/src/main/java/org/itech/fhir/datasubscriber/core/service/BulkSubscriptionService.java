package org.itech.fhir.datasubscriber.core.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.service.CrudService;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;

public interface BulkSubscriptionService extends CrudService<BulkSubscription, Long> {


	BulkSubscription createBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId, URI notificationUri,
			boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(FhirServer sourceServer, FhirResourceGroup fhirResourceGroup,
			URI notificationUri, boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(URI sourceServerUri, Long fhirResourceGroupId, URI notificationUri,
			boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(URI sourceServerUri, String resourceGroupName, URI notificationUri,
			boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException;

	BulkSubscription createOrUpdateBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException;

	BulkSubscription createOrUpdateBulkSubscriptions(FhirServer sourceServer, FhirResourceGroup fhirResourceGroup,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException;

	BulkSubscription createOrUpdateBulkSubscriptions(URI sourceServerUri, Long fhirResourceGroupId, URI notificationUri,
			boolean directSubscription, List<String> headers) throws UnsupportedEncodingException;

	BulkSubscription createOrUpdateBulkSubscriptions(URI sourceServerUri, String resourceGroupName, URI notificationUri,
			boolean directSubscription, List<String> headers) throws UnsupportedEncodingException;

}
