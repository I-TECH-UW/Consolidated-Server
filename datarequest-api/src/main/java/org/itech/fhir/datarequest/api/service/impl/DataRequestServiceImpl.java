package org.itech.fhir.datarequest.api.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.dao.ServerResourceIdMapDAO;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.datarequest.api.service.DataRequestAttemptStatusService;
import org.itech.fhir.datarequest.api.service.DataRequestService;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;
import org.itech.fhir.datarequest.core.service.DataRequestAttemptService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataRequestServiceImpl implements DataRequestService {

	private ServerService serverService;
	private DataRequestAttemptService dataRequestAttemptService;
	private DataRequestAttemptStatusService dataRequestStatusService;
	private FhirContext fhirContext;
	private FhirResourceGroupService fhirResourceGroupService;

	public DataRequestServiceImpl(ServerService serverService, DataRequestAttemptService dataRequestAttemptService,
			DataRequestAttemptStatusService dataRequestStatusService, FhirContext fhirContext,
			FhirResourceGroupService fhirResourceGroupService, ServerResourceIdMapDAO sourceIdToLocalIdDAO) {
		this.serverService = serverService;
		this.dataRequestAttemptService = dataRequestAttemptService;
		this.dataRequestStatusService = dataRequestStatusService;
		this.fhirContext = fhirContext;
		this.fhirResourceGroupService = fhirResourceGroupService;
	}

	@Override
	@Async
	@Transactional
	public Future<List<Bundle>> requestDataFromServerSinceLastSucessfulRequest(Long serverId,
			Long fhirResourceGroupId) {
		return requestDataFromServerFromInstant(serverId, fhirResourceGroupId, dataRequestAttemptService
				.getLatestSuccessInstantForSourceServerAndFhirResourceGroup(serverId, fhirResourceGroupId));
	}

	@Override
	@Async
	@Transactional
	public Future<List<Bundle>> requestDataFromServerFromInstant(Long serverId, Long fhirResourceGroupId,
			Instant lowerBound) {
		log.debug("running dataRequest for server " + serverId);
		DataRequestAttempt dataRequestAttempt = new DataRequestAttempt(serverService.getDAO().findById(serverId).get(),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get());
		dataRequestAttempt = dataRequestAttemptService.getDAO().save(dataRequestAttempt);

		DateRangeParam dateRange = new DateRangeParam().setLowerBoundInclusive(Date.from(lowerBound))
				.setUpperBoundInclusive(Date.from(dataRequestAttempt.getStartTime()));
		return new AsyncResult<>(requestDataFromServer(dataRequestAttempt, fhirResourceGroupId, dateRange));
	}

	// TODO synchronize on the server object if we can guarantee it will be the same
	// object across requests
	@Transactional
	private synchronized List<Bundle> requestDataFromServer(DataRequestAttempt dataRequestAttempt,
			Long fhirResourceGroupId, DateRangeParam dateRange) {
		try {
			List<Bundle> searchBundles = getResourceBundlesFromSourceServer(dataRequestAttempt, dateRange);
			dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(),
					DataRequestStatus.COMPLETE);
			log.debug("finished sending request for server " + dataRequestAttempt.getId());
			return searchBundles;
		} catch (RuntimeException e) {
			log.warn("exception occured while running dataRequest task", e);
		}
		log.error("could not complete a dataRequest task");
		dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(), DataRequestStatus.FAILED);
		return new ArrayList<>();
	}

	private List<Bundle> getResourceBundlesFromSourceServer(DataRequestAttempt dataRequestAttempt,
			DateRangeParam dateRange) {
		Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> fhirResourcesMap = fhirResourceGroupService
				.getAllFhirGroupIdsToResourceSearchParamsGroupedByResourceType();
		log.trace("fhir resource map is: " + fhirResourcesMap);
		String dataRequestName = dataRequestAttempt.getFhirResourceGroup().getResourceGroupName();
		log.debug("data request name is: " + dataRequestName);

		List<Bundle> searchBundles = new ArrayList<>();
		dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(),
				DataRequestStatus.REQUESTED);
		for (Entry<ResourceType, Set<ResourceSearchParam>> resourceSearchParamsSet : fhirResourcesMap
				.get(dataRequestAttempt.getFhirResourceGroup().getId()).entrySet()) {
			Map<String, List<String>> searchParameters = createSearchParams(resourceSearchParamsSet.getKey(),
					resourceSearchParamsSet.getValue());
			IGenericClient sourceFhirClient = fhirContext
					.newRestfulGenericClient(dataRequestAttempt.getServer().getServerUrl().toString());
			Bundle searchBundle = sourceFhirClient//
					.search()//
					.forResource(resourceSearchParamsSet.getKey().name())//
					.whereMap(searchParameters)//
					.lastUpdated(dateRange)//
					.returnBundle(Bundle.class).execute();
			log.trace("received json " + fhirContext.newJsonParser().encodeResourceToString(searchBundle));
			log.debug("received " + searchBundle.getTotal() + " entries of " + resourceSearchParamsSet.getKey());
			searchBundles.add(searchBundle);
			if (Thread.currentThread().isInterrupted()) {
				return searchBundles;
			}
		}
		return searchBundles;
	}

	private Map<String, List<String>> createSearchParams(ResourceType resourceType,
			Set<ResourceSearchParam> resourceSearchParams) {
		Map<String, List<String>> searchParameters = new HashMap<>();

		for (ResourceSearchParam resourceSearchParam : resourceSearchParams) {
			if (resourceSearchParam.getParamName() != null) {
				// here we are 'OR' ing
				searchParameters.put(resourceSearchParam.getParamName(),
						Arrays.asList(String.join(",", resourceSearchParam.getParamValues())));
			}
		}
		log.debug("search parameters for " + resourceType + " are: " + searchParameters);
		return searchParameters;
	}

}
