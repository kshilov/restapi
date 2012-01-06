package com.heymoose.domain.hiber;

import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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

  @Override
  public Iterable<BannerSize> all(boolean activeOnly) {
    Criteria criteria = hiber().createCriteria(BannerSize.class);
    if (activeOnly)
      criteria.add(Restrictions.eq("disabled", false));
    return criteria.list();
  }
}
