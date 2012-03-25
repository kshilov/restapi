package com.heymoose.domain.affiliate;

import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerRepository;
import com.heymoose.domain.BannerStore;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import java.io.File;
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

  private final BannerStore bannerStore;
  private final BannerRepository bannerRepository;
  private final Repo repo;

  @Inject
  public BannerResource(BannerRepository bannerRepository, BannerStore bannerStore, Repo repo) {
    this.bannerRepository = bannerRepository;
    this.bannerStore = bannerStore;
    this.repo = repo;
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long bannerId) throws IOException {
    Banner banner = bannerRepository.byId(bannerId);
    if (banner == null)
      throw notFound();
    File bannerFile = bannerStore.path(bannerId);
    if (bannerFile.exists()) {
      return Response.ok()
          .header("X-Accel-Redirect", "/banners/" + bannerId)
          .header("Content-Type", banner.mimeType())
          .build();
    } else {
      return Response.ok(bannerStore.decodeBase64(banner.imageBase64()))
          .header("Content-Type", banner.mimeType())
          .build();
    }
  }

  public Response findOffers(long siteId) {
    return null;
  }
}
