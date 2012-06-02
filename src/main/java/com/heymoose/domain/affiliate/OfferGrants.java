package com.heymoose.domain.affiliate;

import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfferGrants {

  private final Repo repo;

  @Inject
  public OfferGrants(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public OfferGrant visibleGrant(BaseOffer offer, User affiliate) {
    BaseOffer grantTarget;
    if (offer instanceof Offer)
      grantTarget = offer;
    else if (offer instanceof SubOffer)
      grantTarget = ((SubOffer) offer).parent();
    else
      throw new IllegalStateException();
    OfferGrant grant = repo.byHQL(
        OfferGrant.class,
        "from OfferGrant where offer = ? and affiliate = ?",
        grantTarget, affiliate
    );
    if (grant == null)
      return null;
    if (!grant.offerIsVisible())
      return null;
    return grant;
  }
}