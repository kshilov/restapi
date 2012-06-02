package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.Repository;

public interface OfferRepository extends Repository<Offer> {
  Iterable<Offer> list(Ordering ord, boolean asc, int offset, int limit,
                       Boolean approved, Boolean active, Boolean launched,
                       Long advertiserId);
  long count(Boolean approved, Boolean active, Boolean launched, Long advertiserId);
  
  Iterable<Offer> listRequested(Ordering ord, boolean asc, int offset, int limit,
                                   long affiliateId, Boolean active);
  long countRequested(long affiliateId, Boolean active);
  
  public enum Ordering {
    ID, NAME, URL, ADVERTISER_LAST_NAME,
    GRANT_ID, GRANT_AFFILIATE_LAST_NAME, GRANT_APPROVED, GRANT_ACTIVE
  }
}
