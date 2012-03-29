package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.Repository;

public interface SubOfferRepository extends Repository<SubOffer> {
  Iterable<SubOffer> list(long parentId);
  long count(long parentId);
}
