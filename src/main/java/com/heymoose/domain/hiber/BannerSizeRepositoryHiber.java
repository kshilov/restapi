package com.heymoose.domain.hiber;

import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class BannerSizeRepositoryHiber extends RepositoryHiber<BannerSize> implements BannerSizeRepository {

  @Inject
  public BannerSizeRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<BannerSize> getEntityClass() {
    return BannerSize.class;
  }

  @Override
  public BannerSize byWidthAndHeight(int width, int height) {
    return (BannerSize) hiber().createQuery("from BannerSize where width = :width and height = :height")
        .setParameter("width", width)
        .setParameter("height", height)
        .uniqueResult();
  }
}
