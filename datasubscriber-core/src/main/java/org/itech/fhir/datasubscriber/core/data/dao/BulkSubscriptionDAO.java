package org.itech.fhir.datasubscriber.core.data.dao;

import java.util.List;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.datasubscriber.core.data.model.BulkSubscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSubscriptionDAO extends CrudRepository<BulkSubscription, Long> {

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType")
	List<BulkSubscription> findSubscriptionsWithResourceType(@Param("resourceType") ResourceType resourceType);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType AND bulk.sourceServer.id = :sourceServerId")
	List<BulkSubscription> findSubscriptionsWithResourceTypeAndSubscribeToUri(ResourceType resourceType,
			@Param("sourceServerId") Long sourceServerId);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE bulk.sourceServer.id = :sourceServerId")
	List<BulkSubscription> findSubscriptionsSubscribeToUri(@Param("sourceServerId") Long sourceServerId);

}
