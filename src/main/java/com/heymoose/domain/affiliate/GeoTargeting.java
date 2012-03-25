package com.heymoose.domain.affiliate;

import com.google.common.collect.Sets;
import com.heymoose.domain.Offer;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static java.util.Collections.emptySet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GeoTargeting {
  
  private final Repo repo;

  @Inject
  public GeoTargeting(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Set<Region> regionsByIpNum(long ipNum) {
    IpSegment segment = repo.byHQL(IpSegment.class, "from IpSegment where ? >= startIpNum and ? <= endIpNum", ipNum, ipNum);
    if (segment == null)
      return emptySet();
    return Region.find(segment.code());
  }

  @Transactional
  public boolean isAllowed(Offer offer, long ipNum) {
    return !Sets.intersection(regions(offer), regionsByIpNum(ipNum)).isEmpty();
  }
  
  private static Set<Region> regions(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).regions();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).parent().regions();
    else
      throw new IllegalArgumentException("Unknown offer type: " + offer.getClass().getSimpleName());
  }
}
