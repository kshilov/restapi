package com.heymoose.domain.offer;

import com.heymoose.domain.base.Repository;
import com.heymoose.infrastructure.util.Pair;

import java.util.List;

public interface OfferRepository extends Repository<Offer> {

  Iterable<Offer> list(Ordering ord, boolean asc, int offset, int limit,
                       OfferFilter filter);

  long count(OfferFilter filter);

  Iterable<Offer> listRequested(Ordering ord, boolean asc, int offset, int limit,
                                   long affiliateId, Boolean active);
  long countRequested(long affiliateId, Boolean active);

  Pair<List<Offer>, Long> affiliateOfferList(long affId,
                                             int offset, int limit);

  Offer get(long offerId);

  Iterable<Offer> listProductOffers(Long affId, Long siteId);

  public enum Ordering {
    ID, NAME, URL, ADVERTISER_LAST_NAME,
    GRANT_ID,
    GRANT_AFFILIATE_LAST_NAME, AFFILIATE_ID,
    GRANT_APPROVED, GRANT_ACTIVE
  }
}
