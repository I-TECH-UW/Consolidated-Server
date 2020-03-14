package org.itech.fhir.core.dao;

import org.itech.fhir.core.model.CustomFhirResourceGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomFhirResourceGroupDAO extends CrudRepository<CustomFhirResourceGroup, Long> {

}
