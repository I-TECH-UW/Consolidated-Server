package org.itech.fhir.dataimport.webapp.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.hibernate.ObjectNotFoundException;
import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.itech.fhir.dataimport.webapp.api.dto.CustomFhirResourceGroupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fhirResourceGroup")
public class FhirResourceGroupController {

	private FhirResourceGroupService fhirResourceGroupService;

	public FhirResourceGroupController(FhirResourceGroupService fhirResourceGroupService) {
		this.fhirResourceGroupService = fhirResourceGroupService;
	}

	@GetMapping
	public ResponseEntity<List<FhirResourceGroup>> getFhirResourceGroups() {
		return ResponseEntity.ok(fhirResourceGroupService.getAllFhirGroups());
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<FhirResourceGroup> getFhirResourceGroup(@PathVariable(value = "id") Long id) {
		FhirResourceGroup fhirResourceGroup = fhirResourceGroupService.getDAO().findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(id, FhirResourceGroup.class.getName()));
		return ResponseEntity.ok(fhirResourceGroup);
	}

	@GetMapping(value = "/{id}/resourceSearchParams")
	public ResponseEntity<Set<ResourceSearchParam>> getFhirResourceGroupParamsForFhirResourceGroup(
			@PathVariable(value = "id") Long id) {
		return ResponseEntity.ok(fhirResourceGroupService.getFhirGroupToResourceSearchParams(id).getSecond());
	}

	@GetMapping(value = "/resourceSearchParams")
	public ResponseEntity<Map<FhirResourceGroup, Set<ResourceSearchParam>>> getFhirResourceGroupsWithParams() {
		return ResponseEntity.ok(fhirResourceGroupService.getAllFhirGroupsToResourceSearchParams());
	}

	@PostMapping
	public ResponseEntity<FhirResourceGroup> createFhirResourceGroup(
			@RequestBody @Valid CustomFhirResourceGroupDTO dto) throws URISyntaxException {
		FhirResourceGroup resourceGroup = fhirResourceGroupService
				.createFhirResourceGroup(dto.getResourceGroupName(), dto.getResourceTypesSearchParams());
		return ResponseEntity.created(new URI("/fhirResourceGroup/" + resourceGroup.getId()))
				.body(resourceGroup);
	}

}
