package com.heymoose.domain.affiliate;

import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfferLoader {
  private final Repo repo;

  @Inject
  public OfferLoader(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public BaseOffer findOffer(long advertiserId, String code) {
    SubOffer existentSub = repo.byHQL(
        SubOffer.class,
        "from SubOffer o where o.code = ? and o.parent.advertiser.id = ?",
        code, advertiserId
    );

    if (existentSub != null)
      return existentSub;

    Offer existentOffer = repo.byHQL(
        Offer.class,
        "from Offer o where o.code = ? and o.advertiser.id = ?",
        code, advertiserId
    );

    return existentOffer;
  }
}
