package com.heymoose.domain.grant;

import com.heymoose.domain.user.User;
import com.heymoose.domain.base.Repository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.OfferRepository;

import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate);
  Iterable<OfferGrant> list(OfferRepository.Ordering ord, boolean asc, int offset, int limit,
                            OfferGrantFilter filter);
  long count(OfferGrantFilter filter);
}
