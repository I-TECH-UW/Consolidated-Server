package org.itech.fhir.dataimport.core.dao;

import org.itech.fhir.dataimport.core.model.UnsavedBundles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnsavedBundlesDAO extends CrudRepository<UnsavedBundles, Long> {

}
