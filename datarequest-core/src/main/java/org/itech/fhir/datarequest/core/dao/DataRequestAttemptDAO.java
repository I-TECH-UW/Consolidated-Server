package org.itech.fhir.datarequest.core.dao;

import java.util.List;

import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRequestAttemptDAO extends JpaRepository<DataRequestAttempt, Long> {

	@Query("SELECT dra FROM DataRequestAttempt dra ORDER BY dra.endTime DESC")
	List<DataRequestAttempt> findLatestDataRequestAttempts(Pageable limit);

	@Query("SELECT dra FROM DataRequestAttempt dra WHERE dra.server.id = :serverId  ORDER BY dra.startTime DESC")
	List<DataRequestAttempt> findLatestDataRequestAttemptsByServer(Pageable limit, @Param("serverId") Long serverId);

	@Query("SELECT dra FROM DataRequestAttempt dra WHERE dra.server.id = :serverId AND dra.dataRequestStatus = :dataRequestStatus AND dra.fhirResourceGroup.id = :fhirResourceGroupId ORDER BY dra.startTime DESC")
	List<DataRequestAttempt> findLatestDataRequestAttemptsByServerAndFhirResourceGroupAndStatus(Pageable limit,
			@Param("serverId") Long serverId, @Param("fhirResourceGroupId") Long fhirResourceGroupId,
			@Param("dataRequestStatus") DataRequestStatus dataRequestStatus);
}
