package org.itech.fhir.core.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.dao.FhirResourceGroupDAO;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.ResourceSearchParam;

public interface FhirResourceGroupService extends CrudService<FhirResourceGroup, Long> {

	public enum FhirResourceCategories {
		All,
		// base resources
		Base, Individuals, Entities_1, Entities_2, Workflow, Management,
		// clinical resources
		Clinical, Summary, Diagnostics, Medications, Care_Provision, Request_Response
	}

	Map<FhirResourceGroup, Set<ResourceSearchParam>> getAllFhirGroupsToResourceSearchParams();

	Map<Long, Set<ResourceSearchParam>> getAllFhirGroupIdsToResourceSearchParams();

	Map<Long, Map<ResourceType, Set<ResourceSearchParam>>> getAllFhirGroupIdsToResourceSearchParamsGroupedByResourceType();

	FhirResourceGroup createFhirResourceGroupWithNoSearchParams(String resourceGroupName,
			Set<ResourceType> resourceTypes);

	FhirResourceGroup createFhirResourceGroup(String resourceGroupName,
			Map<ResourceType, Map<String, List<String>>> resourceTypes);

	FhirResourceGroup createFhirResourceGroup(String resourceGroupName,
			Set<ResourceSearchParam> resourceTypes);

	@Override
	FhirResourceGroupDAO getDAO();

}
