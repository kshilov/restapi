package com.heymoose.domain.affiliate;

import com.google.common.collect.Sets;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static java.util.Collections.emptySet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GeoTargeting {
  
  private final Repo repo;
  private final Logger log = LoggerFactory.getLogger(GeoTargeting.class);

  @Inject
  public GeoTargeting(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Set<Region> regionsByIpNum(long ipNum) {
    IpSegment segment = repo.byHQL(IpSegment.class, "from IpSegment where ? >= startIpNum and ? <= endIpNum", ipNum, ipNum);
    if (segment == null) {
      log.warn("Unknown ip: " + intToIp(ipNum) + " [ip num: " + ipNum + "]");
      return emptySet();
    }
    return Region.find(segment.code());
  }

  @Transactional
  public boolean isAllowed(BaseOffer offer, long ipNum) {
    Set<Region> offerRegions = regions(offer);
    if (offerRegions.isEmpty())
      return true;
    Set<Region> ipRegions = regionsByIpNum(ipNum);
    if (ipRegions.isEmpty())
      return true;
    return !Sets.intersection(offerRegions, ipRegions).isEmpty();
  }
  
  private static Set<Region> regions(BaseOffer offer) {
    if (offer instanceof Offer)
      return ((Offer) offer).regions();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).parent().regions();
    else
      throw new IllegalArgumentException("Unknown offer type: " + offer.getClass().getSimpleName());
  }

  public static String intToIp(long i) {
    return ((i >> 24 ) & 0xFF) + "." +
        ((i >> 16 ) & 0xFF) + "." +
        ((i >>  8 ) & 0xFF) + "." +
        ( i        & 0xFF);
  }
}
