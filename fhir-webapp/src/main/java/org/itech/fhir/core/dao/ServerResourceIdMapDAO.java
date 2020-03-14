package org.itech.fhir.core.dao;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.model.ServerResourceIdMap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerResourceIdMapDAO extends CrudRepository<ServerResourceIdMap, Long> {

	@Query("SELECT idMap FROM ServerResourceIdMap idMap WHERE idMap.sourceServer.id = :serverId")
	List<ServerResourceIdMap> findByServerId(@Param("serverId") Long serverId);

	@Query("SELECT idMap FROM ServerResourceIdMap idMap WHERE idMap.sourceServer.id = :serverId AND idMap.resourceType = :resourceType")
	Optional<ServerResourceIdMap> findByServerIdAndResourceType(@Param("serverId") Long serverId,
			@Param("resourceType") ResourceType resourceType);

}
