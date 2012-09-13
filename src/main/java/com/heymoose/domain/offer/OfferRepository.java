package com.heymoose.domain.offer;

import com.heymoose.domain.base.Repository;
import com.heymoose.infrastructure.util.QueryResult;
import org.joda.time.DateTime;

public interface OfferRepository extends Repository<Offer> {

  Iterable<Offer> list(Ordering ord, boolean asc, int offset, int limit,
                       OfferFilter filter);

  long count(OfferFilter filter);

  Iterable<Offer> listRequested(Ordering ord, boolean asc, int offset, int limit,
                                   long affiliateId, Boolean active);
  long countRequested(long affiliateId, Boolean active);

  QueryResult debtGroupedByAffiliate(Offer offer, DateTime from, DateTime to,
                                     int offset, int limit);

  public enum Ordering {
    ID, NAME, URL, ADVERTISER_LAST_NAME,
    GRANT_ID, GRANT_AFFILIATE_LAST_NAME, GRANT_APPROVED, GRANT_ACTIVE
  }
}
