package com.heymoose.infrastructure.offer;

import com.heymoose.domain.model.base.Repo;
import com.heymoose.domain.model.offer.BaseOffer;
import com.heymoose.domain.model.offer.Offer;
import com.heymoose.domain.model.offer.SubOffer;
import com.heymoose.infrastructure.hibernate.Transactional;
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
