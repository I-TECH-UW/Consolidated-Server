package org.itech.fhir.datarequest.api.service.impl;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.datarequest.api.service.DataRequestAttemptStatusService;
import org.itech.fhir.datarequest.api.service.DataRequestService;
import org.itech.fhir.datarequest.core.dao.IncompleteDataRequestDAO;
import org.itech.fhir.datarequest.core.exception.DataRequestFailedException;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt;
import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;
import org.itech.fhir.datarequest.core.model.IncompleteDataRequest;
import org.itech.fhir.datarequest.core.service.DataRequestAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataRequestServiceImpl implements DataRequestService {

	private ServerService serverService;
	private DataRequestAttemptService dataRequestAttemptService;
	private IncompleteDataRequestDAO incompleteDataRequestDAO;
	private DataRequestAttemptStatusService dataRequestStatusService;
	private FhirContext fhirContext;
	private FhirResourceGroupService fhirResourceGroupService;

	@Value("${org.itech.fhir.search.page.max:50}")
	private int searchPageMax;

	public DataRequestServiceImpl(ServerService serverService, DataRequestAttemptService dataRequestAttemptService,
			IncompleteDataRequestDAO incompleteDataRequestDAO, DataRequestAttemptStatusService dataRequestStatusService,
			FhirContext fhirContext, FhirResourceGroupService fhirResourceGroupService) {
		this.serverService = serverService;
		this.dataRequestAttemptService = dataRequestAttemptService;
		this.incompleteDataRequestDAO = incompleteDataRequestDAO;
		this.dataRequestStatusService = dataRequestStatusService;
		this.fhirContext = fhirContext;
		this.fhirResourceGroupService = fhirResourceGroupService;
	}

	@Override
	@Async
	@Transactional
	public Future<List<Bundle>> requestDataFromServerSinceLastSucessfulRequest(Long serverId, Long fhirResourceGroupId)
			throws DataRequestFailedException {
		return requestDataFromServerFromInstant(serverId, fhirResourceGroupId, dataRequestAttemptService
				.getLatestSuccessInstantForSourceServerAndFhirResourceGroup(serverId, fhirResourceGroupId));
	}

	@Override
	@Async
	@Transactional
	public Future<List<Bundle>> requestDataFromServerFromInstant(Long serverId, Long fhirResourceGroupId,
			Instant lowerBound) throws DataRequestFailedException {
		log.debug("running dataRequest for server " + serverId);
		DataRequestAttempt dataRequestAttempt = new DataRequestAttempt(serverService.getDAO().findById(serverId).get(),
				fhirResourceGroupService.getDAO().findById(fhirResourceGroupId).get());
		dataRequestAttempt = dataRequestAttemptService.getDAO().save(dataRequestAttempt);

		DateRangeParam dateRange = new DateRangeParam().setLowerBoundInclusive(Date.from(lowerBound))
				.setUpperBoundInclusive(Date.from(dataRequestAttempt.getStartTime()));
		return new AsyncResult<>(requestDataFromServer(dataRequestAttempt, fhirResourceGroupId, dateRange));
	}

	// TODO add retry mechanisms
	@Transactional
	private synchronized List<Bundle> requestDataFromServer(DataRequestAttempt dataRequestAttempt,
			Long fhirResourceGroupId, DateRangeParam dateRange) throws DataRequestFailedException {
		try {
			List<Bundle> searchBundles = getResourceBundlesFromSourceServer(dataRequestAttempt, dateRange);
			return searchBundles;
		} catch (RuntimeException e) {
			log.warn("exception occured while running dataRequest task", e);
		}
		log.error("could not complete a dataRequest task");
		dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(), DataRequestStatus.FAILED);
		throw new DataRequestFailedException();
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
		boolean timedOut = false;
		Set<ResourceType> unfetchedResources = new HashSet<>();
		for (Entry<ResourceType, Set<ResourceSearchParam>> resourceSearchParamsSet : fhirResourcesMap
				.get(dataRequestAttempt.getFhirResourceGroup().getId()).entrySet()) {
			if (!timedOut) {
				timedOut = searchForResourceType(dataRequestAttempt.getServer().getUri(), resourceSearchParamsSet,
						dateRange, searchBundles).equals(DataRequestStatus.FAILED);
			}
			if (timedOut) {
				unfetchedResources.add(resourceSearchParamsSet.getKey());
			}
		}
		if (!timedOut) {
			dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(),
					DataRequestStatus.SUCCEEDED);
			log.debug("finished sending request for server " + dataRequestAttempt.getId());
		} else {
			dataRequestStatusService.changeDataRequestAttemptStatus(dataRequestAttempt.getId(),
					DataRequestStatus.INCOMPLETE);
			saveIncompleteDataRequest(dataRequestAttempt, unfetchedResources);
		}
		return searchBundles;
	}

	private DataRequestStatus searchForResourceType(URI searchUri,
			Entry<ResourceType, Set<ResourceSearchParam>> resourceSearchParamsSet, DateRangeParam dateRange,
			List<Bundle> searchBundles) {
		if (Thread.currentThread().isInterrupted()) {
			return DataRequestStatus.FAILED;
		}
		Map<String, List<String>> searchParameters = createSearchParams(resourceSearchParamsSet.getKey(),
				resourceSearchParamsSet.getValue());
		IGenericClient sourceFhirClient = fhirContext.newRestfulGenericClient(searchUri.toString());

		Bundle searchBundle = sourceFhirClient//
				.search()//
				.forResource(resourceSearchParamsSet.getKey().name())//
				.whereMap(searchParameters)//
				.count(searchPageMax)//
				.sort(createSortSpec()).lastUpdated(dateRange)//
				.returnBundle(Bundle.class).execute();
		searchBundles.add(searchBundle);
		log.trace("received json " + fhirContext.newJsonParser().encodeResourceToString(searchBundle));
		boolean hasNextPage = searchBundle.getLink(IBaseBundle.LINK_NEXT) != null;
		while (hasNextPage) {
			if (Thread.currentThread().isInterrupted()) {
				return DataRequestStatus.FAILED;
			}
			searchBundle = sourceFhirClient.loadPage().next(searchBundle).execute();
			log.trace("received json " + fhirContext.newJsonParser().encodeResourceToString(searchBundle));
			searchBundles.add(searchBundle);
			hasNextPage = searchBundle.getLink(IBaseBundle.LINK_NEXT) != null;
		}

		log.debug("received " + searchBundle.getTotal() + " entries of " + resourceSearchParamsSet.getKey());
		return DataRequestStatus.SUCCEEDED;
	}

	private SortSpec createSortSpec() {
		SortSpec sortSpec = new SortSpec();
		sortSpec.setParamName("_id");
		return sortSpec;
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

	private void saveIncompleteDataRequest(DataRequestAttempt dataRequestAttempt,
			Set<ResourceType> unfetchedResources) {
		IncompleteDataRequest incompleteDataRequest = new IncompleteDataRequest();
		incompleteDataRequest.setDataRequestAttempt(dataRequestAttempt);
		incompleteDataRequest.setUnfetchedResources(unfetchedResources);
		incompleteDataRequestDAO.save(incompleteDataRequest);
	}

}
