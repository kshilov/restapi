package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface BannerSizeRepository extends Repository<BannerSize> {
  public BannerSize byWidthAndHeight(int width, int height);
}
