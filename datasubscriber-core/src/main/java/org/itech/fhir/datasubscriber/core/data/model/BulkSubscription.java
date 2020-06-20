package org.itech.fhir.datasubscriber.core.data.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.FhirServer;
import org.itech.fhir.core.model.base.PersistenceEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "source_server_id", "notification_uri" }) })
public class BulkSubscription extends PersistenceEntity<Long> {

	// persistence
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(nullable = false, updatable = false)
	// validation
	@NotNull
	private FhirResourceGroup fhirResourceGroup;

	// persistence
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "source_server_id", nullable = false, updatable = false)
	// validation
	@NotNull
	private FhirServer sourceServer;

	// persistence
	@Column(name = "notification_uri")
	// validation
	@NotNull
	private URI notificationUri;

//	// persistence
//	@ElementCollection
//	@MapKeyColumn(name = "resource_type")
//	@Column(name = "subscription_json", length = 65535)
//	@CollectionTable(name = "subscriptions", joinColumns = @JoinColumn(name = "bulk_subscription_id"))
//	// json serialization
//	@JsonIgnore
//	@Transient
//	private Map<ResourceType, Subscription> subscriptions;

	@ElementCollection
	private Map<ResourceType, String> subscriptionLocations;


	public BulkSubscription() {
//		subscriptions = subscriptions == null ? new HashMap<>() : subscriptions;
		subscriptionLocations = subscriptionLocations != null ? subscriptionLocations : new HashMap<>();
	}


	public void putSubscriptionLocation(ResourceType resourceType, String location) {
		subscriptionLocations.put(resourceType, location);
	}

//	public void putSubscription(ResourceType type, Subscription subscription) {
//		subscriptions.put(type, subscription);
//	}


}
