package org.itech.fhir.dataimport.webapp.config;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HttpClientConfig {


	@Value("${server.ssl.trust-store}")
	private Resource trustStore;

	@Value("${server.ssl.trust-store-password}")
	private String trustStorePassword;

	@Value("${server.ssl.key-store}")
	private Resource keyStore;

	@Value("${server.ssl.key-store-password}")
	private String keyStorePassword;

	@Value("${server.ssl.key-password}")
	private String keyPassword;


	public SSLConnectionSocketFactory sslConnectionSocketFactory() throws Exception {
		return new SSLConnectionSocketFactory(sslContext());
	}

	public SSLContext sslContext() throws Exception {
		return SSLContextBuilder.create()
				.loadKeyMaterial(keyStore.getFile(), keyStorePassword.toCharArray(), keyPassword.toCharArray())
				.loadTrustMaterial(trustStore.getFile(), trustStorePassword.toCharArray()).build();
	}

	@Bean
	public CloseableHttpClient httpClient() throws Exception {
		log.debug("creating httpClient");
		return HttpClientBuilder.create()//
				.setSSLSocketFactory(sslConnectionSocketFactory())//
				.build();
	}
}