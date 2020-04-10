package org.itech.fhir.dataimport.api.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.fhir.core.dao.ServerResourceIdMapDAO;
import org.itech.fhir.core.model.ServerResourceIdMap;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.dataimport.api.service.DataSaveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(value = "org.itech.dataimport.forward-notify", havingValue = "false", matchIfMissing = true)
@Slf4j
public class DataForwardSaveServiceImpl implements DataSaveService {

	private ServerService serverService;
	private FhirContext fhirContext;
	private ServerResourceIdMapDAO sourceIdToLocalIdDAO;

	@Value("${org.openelisglobal.fhirstore.uri}")
	private String destinationServerPath;

	public DataForwardSaveServiceImpl(ServerService serverService, FhirContext fhirContext,
			ServerResourceIdMapDAO sourceIdToLocalIdDAO) {
		this.serverService = serverService;
		this.fhirContext = fhirContext;
		this.sourceIdToLocalIdDAO = sourceIdToLocalIdDAO;
	}

	@Override
	public void saveResultsFromServer(Long sourceServerId, List<Bundle> searchBundles) {
		addSearchResultsToLocalServer(sourceServerId, searchBundles);
	}

	private void addSearchResultsToLocalServer(Long sourceServerId, List<Bundle> searchBundles) {
		Bundle transactionBundle = createTransactionBundleFromSearchResults(sourceServerId, searchBundles);
		if (transactionBundle.getTotal() > 0) {
			Bundle transactionResponseBundle = addBundleToLocalServer(transactionBundle);
//			savesourceIdToLocalIdMap(transactionBundle, transactionResponseBundle, sourceServerId);
		}
	}

	private Bundle createTransactionBundleFromSearchResults(Long sourceServerId, List<Bundle> searchResults) {
		Bundle transactionBundle = new Bundle();
		transactionBundle.setType(BundleType.TRANSACTION);
		for (Bundle searchBundle : searchResults) {
			for (BundleEntryComponent searchComponent : searchBundle.getEntry()) {
				if (searchComponent.hasResource()) {
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

//		Optional<String> localId = getResourceLocalId(sourceServerId, sourceResourceId, resourceType);
		// resource already exists on local server, update it with PUT
		transactionComponent.getRequest().setMethod(HTTPVerb.PUT);
		transactionComponent.getRequest().setUrl(resourceType + "/" + sourceResourceId);

		return transactionComponent;
	}

//	private Optional<String> getResourceLocalId(Long sourceServerId, String sourceResourceId,
//			ResourceType resourceType) {
//		log.debug("checking if " + resourceType + " with id " + sourceResourceId + " from source server "
//				+ sourceServerId + " already created on the local  server");
//		Optional<ServerResourceIdMap> serverResourceIdMap = sourceIdToLocalIdDAO
//				.findByServerIdAndResourceType(sourceServerId, resourceType);
//		if (serverResourceIdMap.isEmpty()
//				|| !serverResourceIdMap.get().getSourceIdToLocalIdMap().containsKey(sourceResourceId)) {
//			return Optional.empty();
//		} else {
//			return Optional.of(serverResourceIdMap.get().getSourceIdToLocalIdMap().get(sourceResourceId));
//		}
//	}

	private Bundle addBundleToLocalServer(Bundle transactionBundle) {
		log.debug("sending json " + fhirContext.newJsonParser().encodeResourceToString(transactionBundle));
		IGenericClient destinationFhirClient = fhirContext.newRestfulGenericClient(destinationServerPath);
		Bundle transactionResponseBundle = destinationFhirClient//
				.transaction()//
				.withBundle(transactionBundle)//
				.encodedJson().execute();
		log.debug("response json " + fhirContext.newJsonParser().encodeResourceToString(transactionResponseBundle));
		return transactionResponseBundle;
	}

	private void savesourceIdToLocalIdMap(Bundle transactionBundle, Bundle transactionResponseBundle, Long sourceServerId) {
		for (int i = 0; i < transactionBundle.getTotal(); ++i) {
			BundleEntryComponent transactionBundleEntryComponent = transactionBundle.getEntry().get(i);
			BundleEntryComponent transactionResponseBundleEntryComponent = transactionResponseBundle.getEntry().get(i);
			if (transactionResponseBundleEntryComponent.getResponse().getStatus().contains("201")) {
				log.debug(transactionBundleEntryComponent.getResource().getResourceType() + " with source id "
						+ transactionBundleEntryComponent.getResource().getIdElement().getIdPart() + " and local id "
						+ getIdFromLocation(transactionResponseBundleEntryComponent.getResponse().getLocation()) + " created");
				ServerResourceIdMap sourceIdToLocalId = sourceIdToLocalIdDAO
						.findByServerIdAndResourceType(sourceServerId,
								transactionBundleEntryComponent.getResource().getResourceType())
						.orElse(new ServerResourceIdMap(serverService.getDAO().findById(sourceServerId).get(),
								transactionBundleEntryComponent.getResource().getResourceType()));
				Map<String, String> sourceToLocalMap = sourceIdToLocalId.getSourceIdToLocalIdMap();
				// TODO assuming these are sorted in same order, please confirm
				sourceToLocalMap.put(transactionBundleEntryComponent.getResource().getIdElement().getIdPart(),
						getIdFromLocation(transactionResponseBundleEntryComponent.getResponse().getLocation()));
				sourceIdToLocalIdDAO.save(sourceIdToLocalId);
			} else {
				log.debug("resource wan't created");
			}
		}
	}

	private String getIdFromLocation(String location) {
		int firstSlashIndex = location.indexOf('/');
		return location.substring(firstSlashIndex + 1, location.indexOf('/', firstSlashIndex + 1));
	}

}
