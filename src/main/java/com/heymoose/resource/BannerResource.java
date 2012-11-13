package com.heymoose.resource;

import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.service.BannerStore;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlBanners;

import static com.heymoose.resource.Exceptions.notFound;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@Singleton
@Path("banners")
public class BannerResource {

  private final Repo repo;
  private final BannerStore bannerStore;

  @Inject
  public BannerResource(Repo repo, BannerStore bannerStore) {
    this.repo = repo;
    this.bannerStore = bannerStore;
  }
  
  @GET
  @Transactional
  public XmlBanners list(@QueryParam("offer_id") long offerId,
                         @QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("20") int limit) {
    Offer offer = existingOffer(offerId);
    long count = repo.countByCriteria(DetachedCriteria.forClass(Banner.class)
        .add(Restrictions.eq("offer", offer)));
    Iterable<Banner> banners = repo.pageByCriteria(DetachedCriteria.forClass(Banner.class)
        .add(Restrictions.eq("offer", offer))
        .addOrder(Order.desc("id")), offset, limit);
    return Mappers.toXmlBanners(banners, count);
  }
  
  @DELETE
  @Transactional
  public Response deleteByIds(@QueryParam("offer_id") long offerId,
                              @QueryParam("banner_ids") Set<Long> bannerIds) {
    Offer offer = existingOffer(offerId);
    Map<Long, Banner> banners = repo.get(Banner.class, bannerIds);
    for (Banner banner : banners.values()) {
      if (!banner.offer().equals(offer))
        throw new WebApplicationException(409);
      banner.delete();
    }
    return Response.ok().build();
  }

  @GET
  @Path("local/{id}")
  @Transactional
  public Response getLocal(@PathParam("id") long bannerId) throws IOException {
    Banner banner = repo.get(Banner.class, bannerId);
    if (banner == null)
      throw notFound();
    return Response.ok(bannerStore.getBannerBody(bannerId), banner.mimeType()).build();
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
  
  private Offer existingOffer(long id) {
    Offer offer = repo.get(Offer.class, id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }
}
