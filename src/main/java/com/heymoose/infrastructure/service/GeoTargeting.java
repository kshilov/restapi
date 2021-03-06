package com.heymoose.infrastructure.service;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;

@Singleton
public class GeoTargeting {
  
  private final Repo repo;
  private final Logger log = LoggerFactory.getLogger(GeoTargeting.class);

  @Inject
  public GeoTargeting(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Set<String> regionsByIpNum(long ipNum) {
     List<String> codes = (List<String>) repo.session()
         .createQuery("select countryCode from IpSegment where :ipNum >= startIpNum and :ipNum <= endIpNum")
         .setParameter("ipNum", ipNum)
         .list();
    if (codes.isEmpty()) {
      log.warn("Unknown ip: " + intToIp(ipNum) + " [ip num: " + ipNum + "]");
      return emptySet();
    }
    return newHashSet(codes);
  }

  @Transactional
  public boolean isAllowed(BaseOffer offer, long ipNum) {
    Set<String> offerRegions = offer.regions();
    if (offerRegions.isEmpty())
      return true;
    Set<String> ipRegions = regionsByIpNum(ipNum);
    if (ipRegions.isEmpty())
      return true;
    return !Sets.intersection(offerRegions, ipRegions).isEmpty();
  }

  public static String intToIp(long i) {
    return ((i >> 24 ) & 0xFF) + "." +
        ((i >> 16 ) & 0xFF) + "." +
        ((i >>  8 ) & 0xFF) + "." +
        ( i        & 0xFF);
  }
}
