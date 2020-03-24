package org.itech.fhir.core.service;

public interface ServerLockService {

	Object getLockForSourceServer(Long serverId, String context);

}
