package com.heymoose.infrastructure.service.tracking;

import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.Response;

public interface Tracker {

  Response track(HttpRequestContext context) throws ApiRequestException;
}
