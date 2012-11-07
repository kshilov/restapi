package com.heymoose.infrastructure.service.tracking;

import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.Response;

public final class LidTracker implements Tracker {
  @Override
  public void track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
  }
}
