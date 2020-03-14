package org.itech.fhir.core.dao;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirResourceGroupDAO extends CrudRepository<FhirResourceGroup, Long> {

}
