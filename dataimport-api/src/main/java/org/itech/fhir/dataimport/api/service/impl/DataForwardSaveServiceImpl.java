package org.itech.fhir.dataimport.api.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.dataimport.api.service.DataSaveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataForwardSaveServiceImpl implements DataSaveService {

	private FhirContext fhirContext;

	@Value("${org.itech.fhir.save-server}")
	private String destinationServerPath;

	public DataForwardSaveServiceImpl(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}

	@Override
	public void saveResultsFromServer(Long sourceServerId, List<Bundle> searchBundles) {
		addSearchResultsToLocalServer(sourceServerId, searchBundles);
	}

	private void addSearchResultsToLocalServer(Long sourceServerId, List<Bundle> searchBundles) {
		Bundle transactionBundle = createTransactionBundleFromSearchResults(sourceServerId, searchBundles);
		if (transactionBundle.getTotal() > 0) {
			@SuppressWarnings("unused")
			Bundle transactionResponseBundle = addBundleToDestinationServer(transactionBundle);
		}
	}

	private Bundle createTransactionBundleFromSearchResults(Long sourceServerId, List<Bundle> searchResults) {
		Bundle transactionBundle = new Bundle();
		transactionBundle.setType(BundleType.TRANSACTION);
		for (Bundle searchBundle : searchResults) {
			for (BundleEntryComponent searchComponent : searchBundle.getEntry()) {
				if (searchComponent.hasResource()) {
					// TODO add identifier to always track where it came from
					// addIdentifier(searchComponent.getResource());
					BundleEntryComponent transactionComponent = createTransactionComponentFromSearchComponent(
							searchComponent, sourceServerId);
					transactionBundle.addEntry(transactionComponent);
					transactionBundle.setTotal(transactionBundle.getTotal() + 1);
				}
			}
		}
		return transactionBundle;
	}

	private BundleEntryComponent createTransactionComponentFromSearchComponent(BundleEntryComponent searchComponent,
			Long sourceServerId) {
		ResourceType resourceType = searchComponent.getResource().getResourceType();
		String sourceResourceId = searchComponent.getResource().getIdElement().getIdPart();
		if (StringUtils.isNumeric(sourceResourceId)) {
			throw new IllegalArgumentException("id cannot be a number. Numbers are reserved for local entities only");
		}

		BundleEntryComponent transactionComponent = new BundleEntryComponent();
		transactionComponent.setFullUrl(searchComponent.getFullUrl());
		transactionComponent.setResource(searchComponent.getResource());

		transactionComponent.getRequest().setMethod(HTTPVerb.PUT);
		transactionComponent.getRequest().setUrl(resourceType + "/" + sourceResourceId);

		return transactionComponent;
	}

	private Bundle addBundleToDestinationServer(Bundle transactionBundle) {
		log.debug("sending json " + fhirContext.newJsonParser().encodeResourceToString(transactionBundle));
		IGenericClient destinationFhirClient = fhirContext.newRestfulGenericClient(destinationServerPath);
		Bundle transactionResponseBundle = destinationFhirClient//
				.transaction()//
				.withBundle(transactionBundle)//
				.encodedJson().execute();
		log.debug("response json " + fhirContext.newJsonParser().encodeResourceToString(transactionResponseBundle));
		return transactionResponseBundle;
	}

}
