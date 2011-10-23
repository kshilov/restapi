package com.heymoose.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class Exceptions {

  private Exceptions() {}

  public static WebApplicationException notFound() {
    return new WebApplicationException(Response.Status.NOT_FOUND);
  }

  public static WebApplicationException conflict() {
    return new WebApplicationException(Response.Status.CONFLICT);
  }

  public static WebApplicationException unauthorized() {
    return new WebApplicationException(Response.Status.UNAUTHORIZED);
  }
  
  public static WebApplicationException badRequest() {
    return new WebApplicationException(Response.Status.BAD_REQUEST);
  }
}
