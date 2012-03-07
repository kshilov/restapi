package com.heymoose.domain.affiliate.hiber;

import com.heymoose.domain.affiliate.BannerShow;
import com.heymoose.domain.affiliate.BannerShowRepository;
import com.heymoose.domain.hiber.RepositoryHiber;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class BannerShowRepositoryHiber extends RepositoryHiber<BannerShow> implements BannerShowRepository {

  @Inject
  public BannerShowRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<BannerShow> getEntityClass() {
    return BannerShow.class;
  }
}
