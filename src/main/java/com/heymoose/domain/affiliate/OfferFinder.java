package com.heymoose.domain.affiliate;

import com.heymoose.domain.Offer;
import com.heymoose.domain.affiliate.base.Repo;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfferFinder {
  
  private final Repo repo;

  @Inject
  public OfferFinder(Repo repo) {
    this.repo = repo;
  }
  
  public List<Offer> findOffers(Site site) {
    return null;
  }
}
