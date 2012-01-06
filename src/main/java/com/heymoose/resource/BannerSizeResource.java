package com.heymoose.resource;

import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("banner-sizes")
@Singleton
public class BannerSizeResource {

  private final BannerSizeRepository bannerSizes;

  @Inject
  public BannerSizeResource(BannerSizeRepository bannerSizes) {
    this.bannerSizes = bannerSizes;
  }

  @GET
  @Transactional
  public Response list(@QueryParam("activeOnly") @DefaultValue("false") boolean activeOnly) {
    return Response.ok(Mappers.toXmlBannerSizes(bannerSizes.all(activeOnly))).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("width") Integer width, @FormParam("height") Integer height) {
    BannerSize bannerSize = bannerSizes.byWidthAndHeight(width, height);
    if (bannerSize != null)
      return Response.ok(Long.toString(bannerSize.id())).build();
    bannerSize = new BannerSize(width, height);
    bannerSizes.put(bannerSize);
    return Response.ok(Long.toString(bannerSize.id())).build();
  }
  
  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long sizeId) {
    BannerSize size = bannerSizes.byId(sizeId);
    if (size == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlBannerSize(size, Details.WITH_RELATED_ENTITIES)).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public void disable(@PathParam("id") long sizeId) {
    BannerSize size = bannerSizes.byId(sizeId);
    size.disable();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void enable(@PathParam("id") long sizeId) {
    BannerSize size = bannerSizes.byId(sizeId);
    size.enable();
  }
}
