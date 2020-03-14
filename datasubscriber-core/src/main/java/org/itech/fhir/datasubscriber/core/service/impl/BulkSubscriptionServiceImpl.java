package org.itech.fhir.datasubscriber.core.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
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
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PreferReturnEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSubscriptionServiceImpl extends CrudServiceImpl<BulkSubscription, Long>
		implements BulkSubscriptionService {

	private ServerService serverService;
	private BulkSubscriptionDAO bulkSubscriptionDAO;
	private FhirContext fhirContext;
	private FhirResourceGroupService fhirResourceGroupService;

	@Value("${org.itech.fhir.datasubscriber.receiver.endpoint}")
	private String receiverEndpoint;
	@Value("${org.itech.fhir.datasubscriber.receiver.server-id-param-name}")
	private String sourceServerIdParamName;
	@Value("${org.itech.fhir.datasubscriber.receiver.resource-path-param-name}")
	private String resourcePathParamName;

	public BulkSubscriptionServiceImpl(ServerService serverService, BulkSubscriptionDAO bulkSubscriptionDAO,
			FhirContext fhirContext, FhirResourceGroupService fhirResourceGroupService) {
		super(bulkSubscriptionDAO);
		this.serverService = serverService;
		this.bulkSubscriptionDAO = bulkSubscriptionDAO;
		this.fhirContext = fhirContext;
		this.fhirResourceGroupService = fhirResourceGroupService;
	}

	@Override
	@Transactional
	public BulkSubscription createBulkSubscriptions(Long subscribeToServerId, Long fhirResourceGroupId)
			throws UnsupportedEncodingException {
		return createBulkSubscriptions(serverService.getDAO().findById(subscribeToServerId).get(),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get());
	}

	@Override
	public BulkSubscription createBulkSubscriptions(FhirServer sourceServer, FhirResourceGroup fhirResourceGroup)
			throws UnsupportedEncodingException {
		BulkSubscription bulk = new BulkSubscription();
		bulk.setSourceServer(sourceServer);
		bulk.setFhirResourceGroup(fhirResourceGroup);

		Bundle subscriptionsBundle = createSubscriptionsForType(bulk,
				fhirResourceGroupService.getAllFhirGroupIdsToResourceSearchParamsGroupedByResourceType()
						.get(fhirResourceGroup.getId()));
		Optional<Bundle> receivedSubscriptionsBundleOpt = sendSubscriptionsToRemote(subscriptionsBundle,
				bulk.getSourceServer().getServerUrl());

		if (receivedSubscriptionsBundleOpt.isPresent()) {
			log.debug("received " + receivedSubscriptionsBundleOpt.get());
			for (int i = 0; i < subscriptionsBundle.getEntry().size(); ++i) {
				BundleEntryComponent receivedBundleEntry = receivedSubscriptionsBundleOpt.get().getEntry().get(i);
				BundleEntryComponent bundleEntry = subscriptionsBundle.getEntry().get(i);

				if (bundleEntry.getResource().getResourceType().equals(ResourceType.Subscription)) {
					Subscription subscription = (Subscription) bundleEntry.getResource();
					ResourceType resourceType = extractResourceTypeFromCriteria(subscription.getCriteria());
					bulk.putSubscription(resourceType, subscription);
					bulk.putSubscriptionSuccess(resourceType,
							subscriptionCreatedSuccess(receivedBundleEntry.getResponse()));
				} else {
					log.warn("expected a resource type of \"subscription\" but got "
							+ bundleEntry.getResource().getResourceType());
				}
			}
		}
		log.debug("bulk subscription before saving: " + bulk.toString());
		return bulkSubscriptionDAO.save(bulk);
	}

	private Bundle createSubscriptionsForType(BulkSubscription bulk,
			Map<ResourceType, Set<ResourceSearchParam>> groupedResourceTypeSearchParams)
			throws UnsupportedEncodingException {
		Bundle subscriptionBundle = new Bundle();
		subscriptionBundle.setType(BundleType.TRANSACTION);

		for (Entry<ResourceType, Set<ResourceSearchParam>> groupedResourceSearchParamsSet : groupedResourceTypeSearchParams
				.entrySet()) {
			Subscription subscription = createSubscriptionForType(bulk, groupedResourceSearchParamsSet.getKey(),
					groupedResourceSearchParamsSet.getValue());
			BundleEntryComponent bundleEntry = new BundleEntryComponent();
			bundleEntry.setResource(subscription);
			bundleEntry.setRequest(new BundleEntryRequestComponent().setMethod(HTTPVerb.POST)
					.setUrl(ResourceType.Subscription.name()));

			subscriptionBundle.addEntry(bundleEntry);
		}
		return subscriptionBundle;
	}

	private Subscription createSubscriptionForType(BulkSubscription bulk, ResourceType resourceType,
			Set<ResourceSearchParam> resourceSearchParams) throws UnsupportedEncodingException {
		Subscription subscription = new Subscription();
		subscription.setText(new Narrative().setStatus(NarrativeStatus.GENERATED).setDiv(new XhtmlNode(NodeType.Text)));
		subscription.setStatus(SubscriptionStatus.REQUESTED);
		subscription.setReason("bulk subscription to detect any Creates or Updates to resources of type " + resourceType
				+ " matching params " + resourceSearchParams);

		SubscriptionChannelComponent channel = new SubscriptionChannelComponent();
		channel.setType(SubscriptionChannelType.RESTHOOK).setEndpoint(getEndpointForReceivingServer(bulk));
		channel.setPayload("application/fhir+json");
		subscription.setChannel(channel);

		String criteriaString = createCriteriaString(resourceType, resourceSearchParams);
		subscription.setCriteria(criteriaString);
		return subscription;
	}

	private String getEndpointForReceivingServer(BulkSubscription bulk) throws UnsupportedEncodingException {
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
		log.debug("sending " + subscriptionsBundle + " to " + subscribeToUri);

		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
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

	@SuppressWarnings("unused")
	// used for sending one by one. Replaced by sending by bundle
	private Subscription sendSubscriptionTosource(Subscription subscription, URI subscribeToUri) {
		log.debug("sending " + subscription + " to " + subscribeToUri);
		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
				MethodOutcome outcome = fhirClient.create().resource(subscription)
						.prefer(PreferReturnEnum.REPRESENTATION).encodedJson().execute();
				if (outcome.getCreated()) {
					return (Subscription) outcome.getResource();
				} else {
					log.error("received " + outcome + " while communicating with " + subscribeToUri
							+ " for subscription request ");
				}
			} catch (UnprocessableEntityException | DataFormatException e) {
				log.error(
						"error while communicating subscription request to " + subscribeToUri + " for " + subscription,
						e);
			}
		} catch (URISyntaxException e) {
			log.error("error while creating uri for " + subscribeToUri, e);
		}
		return subscription;
	}

	private ResourceType extractResourceTypeFromCriteria(String criteria) {
		return ResourceType.valueOf(criteria.substring(0, criteria.indexOf("?")));
	}

	private boolean subscriptionCreatedSuccess(BundleEntryResponseComponent response) {
		return response.getStatus().contains("201");
	}

}
