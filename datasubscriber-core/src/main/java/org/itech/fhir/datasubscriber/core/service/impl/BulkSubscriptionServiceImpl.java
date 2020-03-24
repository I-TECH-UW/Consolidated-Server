package org.itech.fhir.datasubscriber.core.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.itech.fhir.core.service.ServerLockService;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.core.service.impl.CrudServiceImpl;
import org.itech.fhir.datasubscriber.core.data.dao.BulkSubscriptionDAO;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.itech.fhir.datasubscriber.core.service.BulkSubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSubscriptionServiceImpl extends CrudServiceImpl<BulkSubscription, Long>
		implements BulkSubscriptionService {

	private static final String LOCK_CONTEXT = "bulkSubscription";

	private ServerService serverService;
	private BulkSubscriptionDAO bulkSubscriptionDAO;
	private FhirContext fhirContext;
	private FhirResourceGroupService fhirResourceGroupService;
	private ServerLockService serverLockService;

	@Value("${org.itech.fhir.datasubscriber.receiver.endpoint}")
	private String receiverEndpoint;
	@Value("${org.itech.fhir.datasubscriber.receiver.server-id-param-name}")
	private String sourceServerIdParamName;
	@Value("${org.itech.fhir.datasubscriber.receiver.resource-path-param-name}")
	private String resourcePathParamName;

	public BulkSubscriptionServiceImpl(ServerService serverService, BulkSubscriptionDAO bulkSubscriptionDAO,
			FhirContext fhirContext, FhirResourceGroupService fhirResourceGroupService,
			ServerLockService serverLockService) {
		super(bulkSubscriptionDAO);
		this.serverService = serverService;
		this.bulkSubscriptionDAO = bulkSubscriptionDAO;
		this.fhirContext = fhirContext;
		this.fhirResourceGroupService = fhirResourceGroupService;
		this.serverLockService = serverLockService;
	}

	@Override
	public BulkSubscription createBulkSubscriptions(URI sourceServerUri, String resourceGroupName, URI notificationUri,
			boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createBulkSubscriptions(serverService.getOrSaveNewServer(sourceServerUri),
				fhirResourceGroupService.getDAO().findResourceGroupWithResourceGroupName(resourceGroupName),
				notificationUri, directSubscription, headers);
	}

	@Override
	@Transactional
	public BulkSubscription createBulkSubscriptions(URI sourceServerUri, Long fhirResourceGroupId, URI notificationUri,
			boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createBulkSubscriptions(serverService.getOrSaveNewServer(sourceServerUri),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get(), notificationUri,
				directSubscription, headers);
	}

	@Override
	@Transactional
	public BulkSubscription createBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createBulkSubscriptions(serverService.getDAO().findById(subscribeToServerId).get(),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get(), notificationUri,
				directSubscription, headers);
	}

	@Override
	public BulkSubscription createBulkSubscriptions(FhirServer sourceServer, FhirResourceGroup fhirResourceGroup,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		BulkSubscription bulk = new BulkSubscription();
		bulk.setSourceServer(sourceServer);
		bulk.setFhirResourceGroup(fhirResourceGroup);
		bulk.setNotificationUri(notificationUri);

		return createSubscriptionsOnSourceServerAndSaveResponse(bulk, fhirResourceGroup, notificationUri,
				directSubscription, headers);
	}

	@Override
	public BulkSubscription createOrUpdateBulkSubscriptions(URI sourceServerUri, String resourceGroupName,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createOrUpdateBulkSubscriptions(serverService.getOrSaveNewServer(sourceServerUri),
				fhirResourceGroupService.getDAO().findResourceGroupWithResourceGroupName(resourceGroupName),
				notificationUri, directSubscription, headers);
	}

	@Override
	@Transactional
	public BulkSubscription createOrUpdateBulkSubscriptions(URI sourceServerUri, Long fhirResourceGroupId,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createOrUpdateBulkSubscriptions(serverService.getOrSaveNewServer(sourceServerUri),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get(), notificationUri,
				directSubscription, headers);
	}

	@Override
	@Transactional
	public BulkSubscription createOrUpdateBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId,
			URI notificationUri, boolean directSubscription, List<String> headers) throws UnsupportedEncodingException {
		return createOrUpdateBulkSubscriptions(serverService.getDAO().findById(subscribeToServerId).get(),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get(), notificationUri,
				directSubscription, headers);
	}

	@Override
	public BulkSubscription createOrUpdateBulkSubscriptions(FhirServer sourceServer,
			FhirResourceGroup fhirResourceGroup, URI notificationUri, boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException {
		synchronized (serverLockService.getLockForSourceServer(sourceServer.getId(), LOCK_CONTEXT)) {
			if (bulkSubscriptionDAO.findSubscriptionsToServerWithNotificationUri(sourceServer.getId(), notificationUri)
					.isEmpty()) {
				log.debug("creating new subscription");
			} else {
				log.debug("subscription found. updating");
			}
			BulkSubscription bulk = bulkSubscriptionDAO
					.findSubscriptionsToServerWithNotificationUri(sourceServer.getId(), notificationUri)
					.orElse(new BulkSubscription());
			bulk.setSourceServer(sourceServer);
			bulk.setFhirResourceGroup(fhirResourceGroup);
			bulk.setNotificationUri(notificationUri);

			removeSubscriptionsFromSourceServer(bulk);
			return createSubscriptionsOnSourceServerAndSaveResponse(bulk, fhirResourceGroup, notificationUri,
					directSubscription, headers);
		}
	}

	private BulkSubscription createSubscriptionsOnSourceServerAndSaveResponse(BulkSubscription bulk,
			FhirResourceGroup fhirResourceGroup, URI notificationUri, boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException {
		Bundle subscriptionsBundle = createSubscriptionsForType(bulk, fhirResourceGroupService
				.getAllFhirGroupIdsToResourceSearchParamsGroupedByResourceType().get(fhirResourceGroup.getId()),
				directSubscription, headers);
		Optional<Bundle> receivedSubscriptionsBundleOpt = sendSubscriptionsToRemote(subscriptionsBundle,
				bulk.getSourceServer().getServerUrl());

		if (receivedSubscriptionsBundleOpt.isPresent()) {
			log.debug("received "
					+ fhirContext.newJsonParser().encodeResourceToString(receivedSubscriptionsBundleOpt.get()));
			for (int i = 0; i < subscriptionsBundle.getEntry().size(); ++i) {
				BundleEntryComponent receivedBundleEntry = receivedSubscriptionsBundleOpt.get().getEntry().get(i);
				BundleEntryComponent sentBundleEntry = subscriptionsBundle.getEntry().get(i);

				if (sentBundleEntry.getResource().getResourceType().equals(ResourceType.Subscription)) {
					Subscription subscription = (Subscription) sentBundleEntry.getResource();
					ResourceType resourceType = extractResourceTypeFromCriteria(subscription.getCriteria());
//					bulk.putSubscription(resourceType, subscription);
					bulk.putSubscriptionLocation(resourceType,
							receivedBundleEntry.getResponse().getLocation());
				} else {
					log.warn("expected a resource type of \"subscription\" but got "
							+ receivedBundleEntry.getResource().getResourceType());
				}
			}
		}
		log.debug("bulk subscription before saving: " + bulk.toString());

		return bulkSubscriptionDAO.save(bulk);
	}

	// TODO check if delete needs to happen
	private void removeSubscriptionsFromSourceServer(BulkSubscription bulk) {
		URI subscribeToUri = bulk.getSourceServer().getServerUrl();
		Bundle subscriptionsDeleteBundle = new Bundle();
		subscriptionsDeleteBundle.setType(BundleType.TRANSACTION);
		for (String subscriptionLocation : bulk.getSubscriptionLocations().values()) {
			BundleEntryComponent bundleEntry = new BundleEntryComponent();
			bundleEntry.setRequest(new BundleEntryRequestComponent().setMethod(HTTPVerb.DELETE)
					.setUrl(subscriptionLocation));
			subscriptionsDeleteBundle.addEntry(bundleEntry);
		}
		log.debug("subscriptionsDeleteBundle: "
				+ fhirContext.newJsonParser().encodeResourceToString(subscriptionsDeleteBundle));

		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
				fhirClient.transaction().withBundle(subscriptionsDeleteBundle).encodedJson().execute();
			} catch (UnprocessableEntityException | DataFormatException e) {
				log.error("error while communicating delete subscriptions bundle to " + subscribeToUri + " for "
						+ subscriptionsDeleteBundle, e);
			}
		} catch (URISyntaxException e) {
			log.error("error while creating uri for " + subscribeToUri, e);
		}
	}

	private Bundle createSubscriptionsForType(BulkSubscription bulk,
			Map<ResourceType, Set<ResourceSearchParam>> groupedResourceTypeSearchParams, boolean directSubscription,
			List<String> headers) throws UnsupportedEncodingException {
		Bundle subscriptionBundle = new Bundle();
		subscriptionBundle.setType(BundleType.TRANSACTION);

		for (Entry<ResourceType, Set<ResourceSearchParam>> groupedResourceSearchParamsSet : groupedResourceTypeSearchParams
				.entrySet()) {
			Subscription subscription = createSubscriptionForType(bulk, groupedResourceSearchParamsSet.getKey(),
					groupedResourceSearchParamsSet.getValue(), directSubscription, headers);
			BundleEntryComponent bundleEntry = new BundleEntryComponent();
			bundleEntry.setResource(subscription);
			bundleEntry.setRequest(new BundleEntryRequestComponent().setMethod(HTTPVerb.POST)
					.setUrl(ResourceType.Subscription.name()));

			subscriptionBundle.addEntry(bundleEntry);
		}
		return subscriptionBundle;
	}

	private Subscription createSubscriptionForType(BulkSubscription bulk, ResourceType resourceType,
			Set<ResourceSearchParam> resourceSearchParams, boolean directSubscription, List<String> headers)
			throws UnsupportedEncodingException {
		Subscription subscription = new Subscription();
		subscription.setText(new Narrative().setStatus(NarrativeStatus.GENERATED).setDiv(new XhtmlNode(NodeType.Text)));
		subscription.setStatus(SubscriptionStatus.REQUESTED);
		subscription.setReason("bulk subscription to detect any Creates or Updates to resources of type " + resourceType
				+ " matching params " + resourceSearchParams);

		SubscriptionChannelComponent channel = new SubscriptionChannelComponent();
		channel.setType(SubscriptionChannelType.RESTHOOK)
				.setEndpoint(getEndpointForReceivingServer(directSubscription, bulk));
		if (headers != null) {
			for (String header : headers) {
				channel.addHeader(header);
			}
		}
		channel.setPayload("application/fhir+json");
		subscription.setChannel(channel);

		String criteriaString = createCriteriaString(resourceType, resourceSearchParams);
		subscription.setCriteria(criteriaString);
		return subscription;
	}

	private String getEndpointForReceivingServer(boolean directSubscription, BulkSubscription bulk)
			throws UnsupportedEncodingException {
		if (directSubscription) {
			log.debug("subscription endpoint is: " + bulk.getNotificationUri().toString());
			return bulk.getNotificationUri().toString();
		}
		StringBuilder sb = new StringBuilder(receiverEndpoint);

		if (!StringUtils.isBlank(sourceServerIdParamName)) {
			sb.append("?");
			sb.append(sourceServerIdParamName);
			sb.append("=");
			sb.append(URLEncoder.encode(bulk.getSourceServer().getId().toString(), "UTF-8"));

			sb.append("&");
			sb.append(resourcePathParamName);
			sb.append("=");
			// when the server responds it appends it's path to the end of the endpoint
			// string, so resourcePathParamName will be filled in by the responding
			// server
		}

		log.debug("subscription endpoint is: " + sb);
		return sb.toString();
	}

	private String createCriteriaString(ResourceType resourceType, Set<ResourceSearchParam> resourceSearchParams) {
		boolean queryString = false;
		String criteriaString = resourceType.name() + "?";
		for (ResourceSearchParam resourceSearchParam : resourceSearchParams) {
			if (resourceSearchParam.getParamName() != null) {
				criteriaString += resourceSearchParam.getParamName() + "="
						+ String.join(",", resourceSearchParam.getParamValues()) + "&";
				queryString = true;
			}
		}
		if (queryString) {
			criteriaString = criteriaString.substring(0, criteriaString.length() - 1);
		}
		log.debug("search criteria string: " + criteriaString);
		return criteriaString;
	}

	private Optional<Bundle> sendSubscriptionsToRemote(Bundle subscriptionsBundle, URI subscribeToUri) {
		log.debug("sending " + fhirContext.newJsonParser().encodeResourceToString(subscriptionsBundle) + " to "
				+ subscribeToUri);

		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
				// TODO get server to return subscription results so we can persist the results
				// instead of the location
				Bundle returnedBundle = fhirClient.transaction().withBundle(subscriptionsBundle).encodedJson()
						.execute();
				return Optional.of(returnedBundle);
			} catch (UnprocessableEntityException | DataFormatException e) {
				log.error("error while communicating subscription bundle to " + subscribeToUri + " for "
						+ subscriptionsBundle, e);
			}
		} catch (URISyntaxException e) {
			log.error("error while creating uri for " + subscribeToUri, e);
		}
		return Optional.empty();
	}

	private ResourceType extractResourceTypeFromCriteria(String criteria) {
		return ResourceType.valueOf(criteria.substring(0, criteria.indexOf("?")));
	}

}
