package org.itech.fhir.proxy.webapp.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.itech.fhir.core.service.ServerService;
import org.itech.fhir.proxy.webapp.config.properties.ConfigProperties;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenHIMMediatorActor extends UntypedActor {

	// TODO we should autowire this but because the mediator engine creates our
	// actors by directly calling the constructor, we aren't able to at this point
	private ServerService serverService;

	private final MediatorConfig config;
    private MediatorHTTPRequest originalRequest;
	private ConfigProperties properties;

	public OpenHIMMediatorActor(MediatorConfig config) {
		this.config = config;
		this.serverService = SpringContext.getBean(ServerService.class);
		this.properties = SpringContext.getBean(ConfigProperties.class);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof MediatorHTTPRequest) {
			processMediatorRequest((MediatorHTTPRequest) msg);
//		} else if (msg instanceof MediatorHTTPResponse) {
//			processMediatorResponse((MediatorHTTPResponse) msg);
		} else {
			unhandled(msg);
		}

	}

	private void processMediatorRequest(MediatorHTTPRequest msg) throws ClientProtocolException, IOException {
		FhirResponse fhirResponse = sendRequestToFhirServer(msg);
		FinishRequest response = new FinishRequest(fhirResponse.responseString, fhirResponse.reponseHeaders,
				fhirResponse.statusCode);
		msg.getRequestHandler().tell(response, getSelf());
//		sendToFhirStore((MediatorHTTPRequest) msg);

	}

	private void processMediatorResponse(MediatorHTTPResponse msg) {
		log.info("Received response from health record service");
        originalRequest.getRespondTo().tell(msg.toFinishRequest(), getSelf());
	}

	private void sendToFhirStore(MediatorHTTPRequest msg) {
		originalRequest = msg;

		ActorSelection connector = getContext().actorSelection(config.userPathFor("http-connector"));
		ConfigProperties properties = SpringContext.getBean(ConfigProperties.class);
		MediatorHTTPRequest serviceRequest = new MediatorHTTPRequest( //
				msg.getRequestHandler(), //
				getSelf(), //
				"Fhir Store", //
				msg.getMethod(), //
				"https", //
				properties.getFhirStoreHost(), //
				properties.getFhirStorePort(), //
				"/hapi-fhir-jpaserver/fhir/", //
				msg.getBody(), //
				msg.getHeaders(), //
				null);
		String serverName = msg.getHeaders().get("Server-Name");
		String serverCode = msg.getHeaders().get("Server-Code");
		if (!GenericValidator.isBlankOrNull(serverCode) || !GenericValidator.isBlankOrNull(serverName)) {
			serverService.receiveNotificationFromServer(serverName, serverCode);
		}
		connector.tell(serviceRequest, getSelf());


	}

	private FhirResponse sendRequestToFhirServer(MediatorHTTPRequest mediatorRequest)
			throws ClientProtocolException, IOException {
		String body = mediatorRequest.getBody();
		Map<String, String> headers = mediatorRequest.getHeaders();
		String method = mediatorRequest.getMethod();
		CloseableHttpClient client = SpringContext.getBean(CloseableHttpClient.class);

		HttpUriRequest httpRequest = null;
		String fhirStorePath = "https://" + properties.getFhirStoreHost() + ":" + properties.getFhirStorePort()
				+ properties.getFhirStorePath();

		if ("GET".equalsIgnoreCase(method)) {
			httpRequest = new HttpGet(fhirStorePath);
		} else if ("POST".equalsIgnoreCase(method)) {
			httpRequest = new HttpPost(fhirStorePath);
			StringEntity stringEntity = new StringEntity(body);
			((HttpEntityEnclosingRequestBase) httpRequest).setEntity(stringEntity);
		} else if ("PUT".equalsIgnoreCase(method)) {
			httpRequest = new HttpPut(fhirStorePath);
			StringEntity stringEntity = new StringEntity(body);
			((HttpEntityEnclosingRequestBase) httpRequest).setEntity(stringEntity);
		}
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpRequest.setHeader(entry.getKey(), entry.getValue());
		}
		try (CloseableHttpResponse response = client.execute(httpRequest)) {
			FhirResponse fhirResponse = new FhirResponse();
			Header[] apacheHeaders = response.getAllHeaders();

			for (Header header : apacheHeaders) {
				fhirResponse.reponseHeaders.put(header.getName(), header.getValue());
			}
			fhirResponse.statusCode = response.getStatusLine().getStatusCode();
			fhirResponse.responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

			return fhirResponse;
		}

	}

	private class FhirResponse {
		Map<String, String> reponseHeaders = new HashMap<>();
		int statusCode;
		String responseString;

	}

}
