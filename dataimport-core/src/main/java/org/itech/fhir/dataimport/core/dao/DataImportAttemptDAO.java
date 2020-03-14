package org.itech.fhir.dataimport.core.dao;

import java.util.List;

import org.itech.fhir.dataimport.core.model.DataImportAttempt;
import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataImportAttemptDAO extends CrudRepository<DataImportAttempt, Long> {

	@Query("SELECT dia FROM DataImportAttempt dia ORDER BY dia.startTime DESC")
	List<DataImportAttempt> findLatestDataImportAttempts(Pageable limit);

	@Query("SELECT dia FROM DataImportAttempt dia WHERE dia.dataImportTask.id = :dataImportTaskId  ORDER BY dia.startTime DESC")
	List<DataImportAttempt> findLatestDataImportAttemptsByDataImportTask(Pageable limit,
			@Param("dataImportTaskId") Long dataImportTaskId);

	@Query("SELECT dia FROM DataImportAttempt dia WHERE dia.dataImportTask.id = :dataImportTaskId AND dia.dataImportStatus = :dataImportStatus ORDER BY dia.startTime DESC")
	List<DataImportAttempt> findLatestDataImportAttemptsByDataImportTaskAndStatus(Pageable limit,
			@Param("dataImportTaskId") Long dataImportTaskId,
			@Param("dataImportStatus") DataImportStatus dataImportStatus);
}
