package org.itech.fhir.proxy.webapp.filter;

import org.itech.fhir.core.service.ServerService;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class UpdateServerFilter extends ZuulFilter {

	ServerService serverService;

	public UpdateServerFilter(ServerService serverService) {
		this.serverService = serverService;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		String serverName = ctx.getRequest().getHeader("Server-Name");
		String serverCode = ctx.getRequest().getHeader("Server-Code");

		serverService.receiveNotificationFromServer(serverName, serverCode);

		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
