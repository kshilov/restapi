package com.heymoose.infrastructure.service;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.persistence.Transactional;
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
