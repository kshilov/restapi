package com.heymoose.resource;

import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
  public Response list() {
    return Response.ok(Mappers.toXmlBannerSizes(bannerSizes.all())).build();
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
}
