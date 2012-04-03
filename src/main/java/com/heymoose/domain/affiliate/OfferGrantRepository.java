package com.heymoose.domain.affiliate;

import com.heymoose.domain.affiliate.OfferRepository.Ordering;
import com.heymoose.domain.base.Repository;
import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                          Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked);
  long count(Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked);
}
