package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.Repository;

public interface NewOfferRepository extends Repository<NewOffer> {
  Iterable<NewOffer> list(Ordering ord, boolean asc, int offset, int limit, Long advertiserId);
  long count(Long advertiserId);
  
  public enum Ordering {
    ID, NAME, URL, ADVERTISER_LAST_NAME  
  }
}
