package org.itech.fhir.dataimport.api.service;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;

public interface DataSaveService {

	void saveResultsFromServer(Long sourceServerId, List<Bundle> searchBundles);


}
