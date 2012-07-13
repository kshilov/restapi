package com.heymoose.domain.model.offer;

import com.heymoose.domain.model.base.Repository;

public interface SubOfferRepository extends Repository<SubOffer> {
  Iterable<SubOffer> list(long parentId);
  long count(long parentId);
}
