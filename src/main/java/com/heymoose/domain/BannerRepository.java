package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface BannerRepository extends Repository<Banner> {
  Iterable<Banner> byOfferIdsAndBannerSize(Iterable<Long> offerIds, BannerSize bannerSize);
}
