package com.heymoose.domain.grant;

import com.heymoose.domain.base.Repository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.OrderingDirection;

import java.util.Map;

public interface OfferGrantRepository extends Repository<OfferGrant> {
  OfferGrant byOfferAndAffiliate(long offerId, long affiliateId);
  Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId);
  OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate);
  Iterable<OfferGrant> list(OfferRepository.Ordering ord,
                            OrderingDirection direction,
                            int offset, int limit,
                            OfferGrantFilter filter);
  Iterable<Offer> exclusiveGrantedOffers(Long affId);
  long count(OfferGrantFilter filter);

  OfferGrant checkGrant(User user, BaseOffer offer);
}
