package org.itech.fhir.dataimport.webapp.api.dto;

import java.net.URI;
import java.util.List;

import lombok.Data;

@Data
public class CreateSubscriptionDTO {

	private String resourceGroupName;

	private URI sourceServerUri;

	private URI notificationUri;

	private Boolean directSubscription;

	private List<String> headers;

}
