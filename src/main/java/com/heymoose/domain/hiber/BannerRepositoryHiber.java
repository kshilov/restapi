package com.heymoose.domain.hiber;

import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class BannerRepositoryHiber extends RepositoryHiber<Banner> implements BannerRepository {

  @Inject
  public BannerRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<Banner> getEntityClass() {
    return Banner.class;
  }
}
