package org.itech.fhir.datarequest.core.dao;

import org.itech.fhir.datarequest.core.model.IncompleteDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncompleteDataRequestDAO extends JpaRepository<IncompleteDataRequest, Long> {

}
