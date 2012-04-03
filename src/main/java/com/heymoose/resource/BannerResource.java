package com.heymoose.resource;

import com.heymoose.domain.Banner;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Singleton
@Path("banners")
public class BannerResource {

  private final Repo repo;

  @Inject
  public BannerResource(Repo repo) {
    this.repo = repo;
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long bannerId) throws IOException {
    Banner banner = repo.get(Banner.class, bannerId);
    if (banner == null)
      throw notFound();
    return Response.ok()
        .header("X-Accel-Redirect", "/banners/" + bannerId)
        .header("Content-Type", banner.mimeType())
        .build();
  }
}
