package org.itech.fhir.core.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.itech.fhir.core.dao.FhirResourceGroupDAO;
import org.itech.fhir.core.dao.ResourceSearchParamDAO;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.springframework.stereotype.Service;

@Service
public class FhirResourceGroupServiceImpl extends CrudServiceImpl<FhirResourceGroup, Long>
		implements FhirResourceGroupService {

	private static final String OE_IDENTIFIER = "3f7d1c6b-2781-4707-847c-03d4cb579470";

	private FhirResourceGroupDAO fhirResourceGroupDAO;
	private ResourceSearchParamDAO resourceSearchParamsDAO;

	public FhirResourceGroupServiceImpl(FhirResourceGroupDAO fhirResourceGroupDAO,
			ResourceSearchParamDAO resourceSearchParamsDAO) {
		super(fhirResourceGroupDAO);
		this.fhirResourceGroupDAO = fhirResourceGroupDAO;
		this.resourceSearchParamsDAO = resourceSearchParamsDAO;

		// this could eventually be moved into a database init script if needed
		if (fhirResourceGroupDAO.count() == 0) {
			Set<ResourceSearchParam> resourceSearchParams = new HashSet<>();

			addOpenMrsBridgeGroup(resourceSearchParams);
			addOpenElisInternalGroup(resourceSearchParams);

			FhirResourceGroup entities1 = fhirResourceGroupDAO
					.save(new FhirResourceGroup(FhirResourceCategories.Entities_1.name()));
			Set<ResourceType> entities1ResourceTypes = new HashSet<>();
			entities1ResourceTypes.add(ResourceType.Organization);
			entities1ResourceTypes.add(ResourceType.OrganizationAffiliation);
			entities1ResourceTypes.add(ResourceType.HealthcareService);
			entities1ResourceTypes.add(ResourceType.Endpoint);
			entities1ResourceTypes.add(ResourceType.Location);
			resourceSearchParams.addAll(createResourceSearchParams(entities1, entities1ResourceTypes));

			FhirResourceGroup workflow = fhirResourceGroupDAO
					.save(new FhirResourceGroup(FhirResourceCategories.Workflow.name()));
			Set<ResourceType> workflowResourceTypes = new HashSet<>();
			workflowResourceTypes.add(ResourceType.Task);
			workflowResourceTypes.add(ResourceType.Appointment);
			workflowResourceTypes.add(ResourceType.AppointmentResponse);
			workflowResourceTypes.add(ResourceType.Schedule);
			workflowResourceTypes.add(ResourceType.VerificationResult);
			resourceSearchParams.addAll(createResourceSearchParams(workflow, workflowResourceTypes));

			FhirResourceGroup base = fhirResourceGroupDAO
					.save(new FhirResourceGroup(FhirResourceCategories.Base.name()));
			Set<ResourceType> baseResourceTypes = new HashSet<>();
			baseResourceTypes.addAll(entities1ResourceTypes);
			baseResourceTypes.addAll(workflowResourceTypes);
			resourceSearchParams.addAll(createResourceSearchParams(base, baseResourceTypes));

			// put all fhirCategoriesToResourceTypes into the all type
			FhirResourceGroup all = fhirResourceGroupDAO.save(new FhirResourceGroup(FhirResourceCategories.All.name()));
			Set<ResourceType> allResourceTypes = resourceSearchParams.stream().map(ResourceSearchParam::getResourceType)
					.collect(Collectors.toSet());
			resourceSearchParams.addAll(createResourceSearchParams(all, allResourceTypes));

			resourceSearchParamsDAO.saveAll(resourceSearchParams);
		}

	}

	private void addOpenMrsBridgeGroup(Set<ResourceSearchParam> resourceSearchParams) {
		FhirResourceGroup openElisOpenMrsBridge = fhirResourceGroupDAO
				.save(new FhirResourceGroup("openElisOpenMrsBridge"));
		resourceSearchParams.add(new ResourceSearchParam(openElisOpenMrsBridge, ResourceType.Task, "status",
				Arrays.asList(TaskStatus.REQUESTED.toCode())));
		resourceSearchParams.add(new ResourceSearchParam(openElisOpenMrsBridge, ResourceType.Task, "owner:Practitioner",
				Arrays.asList(OE_IDENTIFIER)));
		resourceSearchParams.add(new ResourceSearchParam(openElisOpenMrsBridge, ResourceType.ServiceRequest,
				"performer:Practitioner", Arrays.asList(OE_IDENTIFIER)));
	}

	private void addOpenElisInternalGroup(Set<ResourceSearchParam> resourceSearchParams) {
		FhirResourceGroup openElisInternalFhir = fhirResourceGroupDAO
				.save(new FhirResourceGroup("openElisInternalApi"));
		resourceSearchParams.add(new ResourceSearchParam(openElisInternalFhir, ResourceType.Task, "status",
				Arrays.asList(TaskStatus.REQUESTED.toCode())));

	}

	private Set<ResourceSearchParam> createResourceSearchParams(FhirResourceGroup fhirResourceGroup,
			Set<ResourceType> resourceTypes) {
		Set<ResourceSearchParam> resourceSearchParams = new HashSet<>();
		for (ResourceType resourceType : resourceTypes) {
			resourceSearchParams.add(new ResourceSearchParam(fhirResourceGroup, resourceType));
		}
		return resourceSearchParams;
	}

	// a replacement for a bidirectional relationship
	@Override
	public Map<FhirResourceGroup, Set<ResourceSearchParam>> getAllFhirGroupsToResourceSearchParams() {
		Map<FhirResourceGroup, Set<ResourceSearchParam>> fhirGroupsToResourceTypes = new HashMap<>();
		for (FhirResourceGroup fhirResourceGroup : fhirResourceGroupDAO.findAll()) {
			fhirGroupsToResourceTypes.put(fhirResourceGroup,
					resourceSearchParamsDAO.findAllForFhirResourceGroup(fhirResourceGroup.getId()));
		}
		return fhirGroupsToResourceTypes.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> Set.copyOf(e.getValue())));
	}

	@Override
	public Map<Long, Set<ResourceSearchParam>> getAllFhirGroupIdsToResourceSearchParams() {
		Map<Long, Set<ResourceSearchParam>> fhirGroupsToResourceTypes = new HashMap<>();
		for (FhirResourceGroup fhirResourceGroup : fhirResourceGroupDAO.findAll()) {
			fhirGroupsToResourceTypes.put(fhirResourceGroup.getId(),
					resourceSearchParamsDAO.findAllForFhirResourceGroup(fhirResourceGroup.getId()));
		}
		return fhirGroupsToResourceTypes;
	}

	@Override
	public Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> getAllFhirGroupIdsToResourceSearchParamsGroupedByResourceType() {
		Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> allFhirGroupsToResourceTypesGrouped = new HashMap<>();
		allFhirGroupsToResourceTypesGrouped
				.putAll(groupResourceSearchParams(getAllFhirGroupIdsToResourceSearchParams()));
		return allFhirGroupsToResourceTypesGrouped;
	}

	private Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> groupResourceSearchParams(
			Map<Long, Set<ResourceSearchParam>> fhirGroupsToResourceTypes) {
		Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> fhirGroupsToResourceTypesGrouped = new HashMap<>();
		// for each FhirResourceGroup entry
		for (Entry<Long, Set<ResourceSearchParam>> entrySet : fhirGroupsToResourceTypes.entrySet()) {
			// create a map of ResourceType to ResourceSearchParams
			Map<ResourceType, Set<ResourceSearchParam>> groupedResourceTypes = new HashMap<>();
			// for each ResourceSearchParams
			for (ResourceSearchParam resourceSearchParams : entrySet.getValue()) {
				// find the current set of ResourceSearchParams
				Set<ResourceSearchParam> groupedSearchParams = groupedResourceTypes
						.get(resourceSearchParams.getResourceType());
				// if the set is null, instantiate it and store it in the map of resourceType to
				// ResourceSearchParams
				if (groupedSearchParams == null) {
					groupedSearchParams = new HashSet<>();
					groupedResourceTypes.put(resourceSearchParams.getResourceType(), groupedSearchParams);
				}
				// add the current ResourceSearchParams to the set of ResourceSearchParams for
				// this ResourceType
				groupedSearchParams.add(resourceSearchParams);
			}
			// add the Set of ResourceSearchParams grouped by ResourceType for the current
			// FhirResourceGroup
			fhirGroupsToResourceTypesGrouped.put(entrySet.getKey(), groupedResourceTypes);
		}
		// return all
		return fhirGroupsToResourceTypesGrouped;
	}

	@Override
	public FhirResourceGroup createFhirResourceGroupWithNoSearchParams(String resourceGroupName,
			Set<ResourceType> resourceTypes) {
		FhirResourceGroup newResourceGroup = new FhirResourceGroup(resourceGroupName);
		newResourceGroup = fhirResourceGroupDAO.save(newResourceGroup);
		for (ResourceType resourceType : resourceTypes) {
			resourceSearchParamsDAO.save(new ResourceSearchParam(newResourceGroup, resourceType));
		}
		return newResourceGroup;
	}

	@Override
	public FhirResourceGroup createFhirResourceGroup(String resourceGroupName,
			Map<ResourceType, Map<String, List<String>>> resourceTypesSearchParams) {
		FhirResourceGroup newResourceGroup = new FhirResourceGroup(resourceGroupName);
		newResourceGroup = fhirResourceGroupDAO.save(newResourceGroup);
		for (Entry<ResourceType, Map<String, List<String>>> resourceTypeSearchParams : resourceTypesSearchParams
				.entrySet()) {
			for (Entry<String, List<String>> searchParam : resourceTypeSearchParams.getValue().entrySet()) {
				resourceSearchParamsDAO.save(new ResourceSearchParam(newResourceGroup,
						resourceTypeSearchParams.getKey(), searchParam.getKey(), searchParam.getValue()));
			}
		}
		return newResourceGroup;
	}

	@Override
	public FhirResourceGroup createFhirResourceGroup(String resourceGroupName, Set<ResourceSearchParam> resourceTypes) {
		FhirResourceGroup newResourceGroup = new FhirResourceGroup(resourceGroupName);
		newResourceGroup = fhirResourceGroupDAO.save(newResourceGroup);
		for (ResourceSearchParam resourceType : resourceTypes) {
			resourceType.setFhirResourceGroup(newResourceGroup);
		}
		resourceSearchParamsDAO.saveAll(resourceTypes);
		return newResourceGroup;
	}

	@Override
	public FhirResourceGroupDAO getDAO() {
		return fhirResourceGroupDAO;
	}

}
