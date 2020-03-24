package org.itech.fhir.core.dao;

import java.net.URI;
import java.util.Optional;

import org.itech.fhir.core.model.FhirServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerDAO extends JpaRepository<FhirServer, Long> {

	@Query("SELECT s FROM FhirServer s WHERE s.name = :name")
	Optional<FhirServer> findByName(@Param("name") String name);

	@Query("SELECT s FROM FhirServer s WHERE s.serverUrl = :serverUrl")
	Optional<FhirServer> findByAddress(@Param("serverUrl") URI serverUrl);

}
