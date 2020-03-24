package org.itech.fhir.core.dao;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirResourceGroupDAO extends CrudRepository<FhirResourceGroup, Long> {

	@Query("SELECT rg FROM FhirResourceGroup rg WHERE rg.resourceGroupName = :resourceGroupName")
	FhirResourceGroup findResourceGroupWithResourceGroupName(@Param("resourceGroupName") String resourceGroupName);
}
