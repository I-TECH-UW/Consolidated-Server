package org.itech.fhir.datasubscriber.core.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.datasubscriber.core.data.dao.BulkSubscriptionDAO;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.itech.fhir.datasubscriber.core.service.SubscriptionEventNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriptionEventNotificationServiceImpl implements SubscriptionEventNotificationService {

	private FhirContext fhirContext;
	private BulkSubscriptionDAO bulkSubscriptionDAO;
	private CloseableHttpClient httpClient;

	@Value("${org.itech.fhir.datasubscriber.notify-server.address}")
	private String notifyServerAddress;

	public SubscriptionEventNotificationServiceImpl(FhirContext fhirContext, BulkSubscriptionDAO bulkSubscriptionDAO,
			CloseableHttpClient httpClient) {
		this.fhirContext = fhirContext;
		this.bulkSubscriptionDAO = bulkSubscriptionDAO;
		this.httpClient = httpClient;
	}

	@Override
	@Async
	public void notifyForResourceFromSource(URI resourcePath, Long sourceServerId) {
		ResourceType resourceType = ResourceType.valueOf(getResourceType(resourcePath));
		notifyForResourceTypeFromSource(resourceType, sourceServerId);
	}

	private String getResourceType(URI resourcePath) {
		log.debug("getting resource type from: " + resourcePath);
//		return Pattern.compile("[\\/]?([a-zA-Z]+)\\/.*").matcher(resourcePath.toString()).group(1);
		String resourcePathString = resourcePath.toString();
		int firstSlashIndex = resourcePathString.indexOf('/');
		int secondSlashIndex = resourcePathString.indexOf('/', firstSlashIndex + 1);
		return resourcePath.toString().substring(firstSlashIndex + 1, secondSlashIndex);
	}

	@SuppressWarnings("unused")
	private String getResourceType(String jsonResource) {
		return fhirContext.newJsonParser().parseResource(jsonResource).fhirType();
	}

	@Override
	@Async
	public void notifyForResourceTypeFromSource(ResourceType resourceType, Long sourceServerId) {
		List<BulkSubscription> bulks = bulkSubscriptionDAO
				.findSubscriptionsWithResourceTypeAndSubscribeToUri(resourceType, sourceServerId);
		for (BulkSubscription bulk : bulks) {
			notifySubscriberOfResourceType(resourceType, bulk);
		}
	}

	private void notifySubscriberOfResourceType(ResourceType resourceType, BulkSubscription bulk) {
		log.debug("notifying server of " + resourceType);
		HttpPost httpPost = new HttpPost(notifyServerAddress + "/server/" + bulk.getSourceServer().getId());
		log.debug("notify server address " + httpPost.getURI());
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			switch (httpResponse.getStatusLine().getStatusCode()) {
			case 200:
			case 201:
				log.debug("successfully contacted notify server");
				break;
			default:
				log.error("did not receive success message from notify server. Received httpResponse "
						+ httpResponse.getStatusLine().getStatusCode());
			}

			return;
		} catch (IOException e) {
			log.error("error occured notifying subscriber at " + notifyServerAddress, e);
		}
	}

	@Override
	@Async
	public void notifyFromSource(Long sourceServerId) {
		List<BulkSubscription> bulks = bulkSubscriptionDAO
				.findSubscriptionsSubscribeToUri(sourceServerId);
		for (BulkSubscription bulk : bulks) {
			notifySubscriber(bulk);
		}
	}

	private void notifySubscriber(BulkSubscription bulk) {
		log.debug("notifying server of a resource change");
		HttpPost httpPost = new HttpPost(notifyServerAddress + "/server/" + bulk.getSourceServer().getId());
		log.debug("notify server address " + httpPost.getURI());
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			switch (httpResponse.getStatusLine().getStatusCode()) {
			case 200:
			case 201:
				log.debug("successfully contacted notify server");
				break;
			default:
				log.error("did not receive success message from notify server. Received httpResponse "
						+ httpResponse.getStatusLine().getStatusCode());
			}

			return;
		} catch (IOException e) {
			log.error("error occured notifying subscriber at " + notifyServerAddress, e);
		}
	}

}
