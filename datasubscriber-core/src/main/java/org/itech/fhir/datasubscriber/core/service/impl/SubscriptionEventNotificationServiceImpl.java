package org.itech.fhir.datasubscriber.core.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.datasubscriber.core.data.dao.BulkSubscriptionDAO;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.itech.fhir.datasubscriber.core.service.SubscriptionEventNotificationService;
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
		BasicNameValuePair param1 = new BasicNameValuePair("serverId", bulk.getSourceServer().getId().toString());
		HttpEntity entity;
		try {
			entity = new UrlEncodedFormEntity(Arrays.asList(param1));
		} catch (UnsupportedEncodingException e) {
			log.error("error occured notifying subscriber at " + bulk.getNotificationUri(), e);
			return;
		}

		log.debug("notifying server of a resource change");
		HttpPost httpPost = new HttpPost(bulk.getNotificationUri() + "/" + resourceType);
		httpPost.setEntity(entity);

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
			log.error("error occured notifying subscriber at " + bulk.getNotificationUri(), e);
		}
	}

	@Override
	@Async
	public void notifyFromSource(Long sourceServerId) {
		List<BulkSubscription> bulks = bulkSubscriptionDAO.findSubscriptionsToServer(sourceServerId);
		for (BulkSubscription bulk : bulks) {
			notifySubscriber(bulk);
		}
	}

	private void notifySubscriber(BulkSubscription bulk) {
		BasicNameValuePair param1 = new BasicNameValuePair("serverId", bulk.getSourceServer().getId().toString());
		HttpEntity entity;
		try {
			entity = new UrlEncodedFormEntity(Arrays.asList(param1));
		} catch (UnsupportedEncodingException e) {
			log.error("error occured notifying subscriber at " + bulk.getNotificationUri(), e);
			return;
		}

		log.debug("notifying server of a resource change");
		HttpPost httpPost = new HttpPost(bulk.getNotificationUri());
		httpPost.setEntity(entity);
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
			log.error("error occured notifying subscriber at " + bulk.getNotificationUri(), e);
		}
	}

}
