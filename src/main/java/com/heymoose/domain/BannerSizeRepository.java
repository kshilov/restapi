package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface BannerSizeRepository extends Repository<BannerSize> {
  BannerSize byWidthAndHeight(int width, int height);
  Iterable<BannerSize> all(boolean activeOnly);
}
