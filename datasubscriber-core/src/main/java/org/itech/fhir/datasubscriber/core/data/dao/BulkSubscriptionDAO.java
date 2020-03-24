package org.itech.fhir.datasubscriber.core.data.dao;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSubscriptionDAO extends CrudRepository<BulkSubscription, Long> {

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptionLocations s WHERE KEY(s) = :resourceType")
	List<BulkSubscription> findSubscriptionsWithResourceType(@Param("resourceType") ResourceType resourceType);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptionLocations s WHERE KEY(s) = :resourceType AND bulk.sourceServer.id = :sourceServerId")
	List<BulkSubscription> findSubscriptionsWithResourceTypeAndSubscribeToUri(ResourceType resourceType,
			@Param("sourceServerId") Long sourceServerId);

	@Query("SELECT bulk FROM BulkSubscription bulk WHERE bulk.sourceServer.id = :sourceServerId")
	List<BulkSubscription> findSubscriptionsToServer(@Param("sourceServerId") Long sourceServerId);

	@Query("SELECT bulk FROM BulkSubscription bulk WHERE bulk.sourceServer.id = :sourceServerId AND bulk.notificationUri = :notificationUri")
	Optional<BulkSubscription> findSubscriptionsToServerWithNotificationUri(
			@Param("sourceServerId") Long sourceServerId,
			@Param("notificationUri") URI notificationUri);

}
