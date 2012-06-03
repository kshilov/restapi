package com.heymoose.domain.affiliate;

import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.OfferRepository.Ordering;
import com.heymoose.domain.base.Repository;
import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate);
  Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                          Long offerId, Long affiliateId, OfferGrantState state,
                          Boolean blocked, Boolean moderation);
  long count(Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked, Boolean moderation);
}
