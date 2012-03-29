package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.Repository;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                          Long offerId, Long affiliateId, Boolean approved, Boolean active);
  long count(Long offerId, Long affiliateId, Boolean approved, Boolean active);
  
  public enum Ordering {
    ID, OFFER_NAME, AFFILIATE_LAST_NAME, APPROVED, ACTIVE 
  }
}
