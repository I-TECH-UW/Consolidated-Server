package org.itech.fhir.core.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.itech.fhir.core.service.ServerLockService;
import org.springframework.stereotype.Service;

@Service
public class ServerLockServiceImpl implements ServerLockService {

	private ConcurrentMap<String, ConcurrentMap<Long, Object>> contextServerIdLocks = new ConcurrentHashMap<>();

	@Override
	public Object getLockForSourceServer(Long serverId, String context) {
		contextServerIdLocks.putIfAbsent(context,
				contextServerIdLocks.getOrDefault(context, new ConcurrentHashMap<>()));
		ConcurrentMap<Long, Object> serverIdLocks = contextServerIdLocks.get(context);
		contextServerIdLocks.put(context, serverIdLocks);
		serverIdLocks.putIfAbsent(serverId, serverId);
		return serverIdLocks.get(serverId);
	}
}
