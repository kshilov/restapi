package com.heymoose.domain.affiliate.repository;

import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.BaseOffer;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.base.Repository;

import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate);
  Iterable<OfferGrant> list(OfferRepository.Ordering ord, boolean asc, int offset, int limit,
                            OfferGrantFilter filter);
  long count(OfferGrantFilter filter);
}
