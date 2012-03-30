package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.Repository;

public interface NewOfferRepository extends Repository<NewOffer> {
  Iterable<NewOffer> list(Ordering ord, boolean asc, int offset, int limit,
                          Boolean approved, Boolean active, Long advertiserId);
  long count(Boolean approved, Boolean active, Long advertiserId);
  
  Iterable<NewOffer> listRequested(Ordering ord, boolean asc, int offset, int limit,
                                   long affiliateId, Boolean active);
  long countRequested(long affiliateId, Boolean active);
  
  public enum Ordering {
    ID, NAME, URL, ADVERTISER_LAST_NAME,
    GRANT_ID, GRANT_AFFILIATE_LAST_NAME, GRANT_APPROVED, GRANT_ACTIVE
  }
}
