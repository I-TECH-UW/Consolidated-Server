package org.itech.fhir.core.web.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.itech.fhir.core.model.FhirResourceGroup;
import org.itech.fhir.core.model.ResourceSearchParam;
import org.itech.fhir.core.service.FhirResourceGroupService;
import org.itech.fhir.core.web.dto.CustomFhirResourceGroupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	public ResponseEntity<Map<FhirResourceGroup, Set<ResourceSearchParam>>> getAllFhirResourceGroups() {
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
