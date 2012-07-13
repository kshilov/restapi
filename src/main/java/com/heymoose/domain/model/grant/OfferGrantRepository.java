package com.heymoose.domain.model.grant;

import com.heymoose.domain.model.User;
import com.heymoose.domain.model.base.Repository;
import com.heymoose.domain.model.offer.BaseOffer;
import com.heymoose.domain.model.offer.OfferRepository;

import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate);
  Iterable<OfferGrant> list(OfferRepository.Ordering ord, boolean asc, int offset, int limit,
                            OfferGrantFilter filter);
  long count(OfferGrantFilter filter);
}
