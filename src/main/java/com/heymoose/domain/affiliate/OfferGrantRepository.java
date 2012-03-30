package com.heymoose.domain.affiliate;

import java.util.Map;

import com.heymoose.domain.affiliate.NewOfferRepository.Ordering;
import com.heymoose.domain.base.Repository;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                          Long offerId, Long affiliateId, Boolean approved, Boolean active);
  long count(Long offerId, Long affiliateId, Boolean approved, Boolean active);
}
