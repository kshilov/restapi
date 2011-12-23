package com.heymoose.domain.hiber;

import static com.google.common.collect.Iterables.isEmpty;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerRepository;
import com.heymoose.domain.BannerSize;
import java.util.List;
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

  @Override
  public Iterable<Banner> byOfferIdsAndBannerSize(Iterable<Long> offerIds, BannerSize bannerSize) {
    if (isEmpty(offerIds))
      return newArrayList();
    List<Banner> banners = hiber().createQuery("from Banner where offer.id in :ids and size = :size")
        .setParameterList("ids", Lists.newArrayList(offerIds))
        .setParameter("size", bannerSize)
        .list();
    return banners;
  }
}
