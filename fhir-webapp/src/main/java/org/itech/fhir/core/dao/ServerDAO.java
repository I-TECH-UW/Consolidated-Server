package org.itech.fhir.core.dao;

import java.util.Optional;

import org.itech.fhir.core.model.FhirServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerDAO extends JpaRepository<FhirServer, Long> {

	Optional<FhirServer> findByName(String name);

}
