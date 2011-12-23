package com.heymoose.domain.hiber;

import com.google.common.collect.Maps;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BannerCacher {

  private final OfferRepository offers;

  @Inject
  public BannerCacher(OfferRepository offers) {
    this.offers = offers;
  }

  public void suggest(Set<Long> ids) {
    Set<Offer> banners = offers.byIds(ids);
  }

  public BannerOffer get(Long id) {
    return cache.get().get(id);
  }

  public void clear() {
    cache.set(Maps.<Long, BannerOffer>newHashMap());
  }

  private ThreadLocal<Map<Long, BannerOffer>> cache = new ThreadLocal<Map<Long, BannerOffer>>() {
    @Override
    protected Map<Long, BannerOffer> initialValue() {
      return newHashMap();
    }
  };
}
