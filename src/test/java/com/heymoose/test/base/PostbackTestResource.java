package com.heymoose.test.base;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Serg Prasolov ci.serg@gmail.com
 * @since 6/5/12 8:43 PM
 */
@Path("postback")
@Singleton
public class PostbackTestResource {

  private final static Logger log = LoggerFactory.getLogger(PostbackTestResource.class);

  @GET
  public Response call(@Context UriInfo ui) {
    //MultivaluedMap<String,String> queryParameters = ui.getQueryParameters();
    log.debug("Postback REQUEST: " + ui.getRequestUri());
    return Response.ok().build();

  }


}
