package com.heymoose.domain.hiber;

import com.heymoose.domain.OfferShow;
import com.heymoose.domain.OfferShowRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class OfferShowRepositoryHiber extends RepositoryHiber<OfferShow> implements OfferShowRepository {

  @Inject
  public OfferShowRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<OfferShow> getEntityClass() {
    return OfferShow.class;
  }
}
