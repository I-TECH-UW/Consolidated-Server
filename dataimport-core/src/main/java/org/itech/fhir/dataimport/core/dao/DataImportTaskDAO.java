package org.itech.fhir.dataimport.core.dao;

import java.util.Optional;

import org.itech.fhir.dataimport.core.model.DataImportTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataImportTaskDAO extends JpaRepository<DataImportTask, Long> {

	@Query("SELECT it FROM DataImportTask it WHERE it.sourceServer.id = :serverId")
	Optional<DataImportTask> findDataImportTaskFromServer(@Param("serverId") Long id);

}
