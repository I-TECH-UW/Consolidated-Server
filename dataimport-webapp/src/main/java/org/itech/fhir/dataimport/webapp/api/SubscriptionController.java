package org.itech.fhir.datasubscriber.webapp.web.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.Valid;

import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.itech.fhir.datasubscriber.core.service.BulkSubscriptionService;
import org.itech.fhir.datasubscriber.webapp.web.api.dto.CreateSubscriptionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("subscription")
public class SubscriptionController {

	private BulkSubscriptionService subscriptionService;

	public SubscriptionController(BulkSubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping
	public ResponseEntity<Iterable<BulkSubscription>> getAllSubscriptions() throws UnsupportedEncodingException {
		return ResponseEntity.ok(subscriptionService.getDAO().findAll());
	}

	@GetMapping("/{bulkId}")
	public ResponseEntity<BulkSubscription> getSubscription(@PathVariable("bulkId") Long bulkId)
			throws UnsupportedEncodingException {
		return ResponseEntity.ok(subscriptionService.getDAO().findById(bulkId).get());
	}

	@PostMapping("/server/{serverId}/fhirResourceGroup/{resourceGroupId}")
	public ResponseEntity<BulkSubscription> createSubscription(@PathVariable("serverId") Long serverId,
			@PathVariable("resourceGroupId") Long resourceGroupId, @RequestParam("notificationUri") URI notificationUri)
			throws UnsupportedEncodingException, URISyntaxException {
		BulkSubscription subscription = subscriptionService.createBulkSubscriptions(serverId, resourceGroupId,
				notificationUri, false, null);
		return ResponseEntity.created(new URI("/subscription/" + subscription.getId())).body(subscription);
	}

	@PostMapping("/fhirResourceGroup/{resourceGroupId}")
	public ResponseEntity<BulkSubscription> createSubscription(@PathVariable("resourceGroupId") Long resourceGroupId,
			@RequestParam("sourceServerUri") URI sourceServerUri, @RequestParam("notificationUri") URI notificationUri)
			throws UnsupportedEncodingException, URISyntaxException {
		BulkSubscription subscription = subscriptionService.createBulkSubscriptions(sourceServerUri, resourceGroupId,
				notificationUri, false, null);
		return ResponseEntity.created(new URI("/subscription/" + subscription.getId())).body(subscription);
	}

	@PostMapping("/fhirResourceGroup")
	public ResponseEntity<BulkSubscription> createSubscriptionByGroupName(@Valid CreateSubscriptionDTO dto)
			throws UnsupportedEncodingException, URISyntaxException {
		BulkSubscription subscription = subscriptionService.createBulkSubscriptions(dto.getSourceServerUri(),
				dto.getResourceGroupName(), dto.getNotificationUri(), dto.getDirectSubscription(), dto.getHeaders());
		return ResponseEntity.created(new URI("/subscription/" + subscription.getId())).body(subscription);
	}

	@PutMapping("/fhirResourceGroup")
	public ResponseEntity<BulkSubscription> createOrUpdateSubscriptionByGroupName(@Valid CreateSubscriptionDTO dto)
			throws UnsupportedEncodingException, URISyntaxException {
		subscriptionService.createOrUpdateBulkSubscriptions(dto.getSourceServerUri(),
				dto.getResourceGroupName(), dto.getNotificationUri(), dto.getDirectSubscription(), dto.getHeaders());
		return ResponseEntity.accepted().build();
	}

}
