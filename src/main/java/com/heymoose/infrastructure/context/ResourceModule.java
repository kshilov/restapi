package com.heymoose.infrastructure.context;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.heymoose.infrastructure.util.CacheInterceptor;
import com.heymoose.infrastructure.util.Cacheable;
import com.heymoose.resource.AccountResource;
import com.heymoose.resource.BannerResource;
import com.heymoose.resource.CategoryResource;
import com.heymoose.resource.ErrorInfoResource;
import com.heymoose.resource.MlmResource;
import com.heymoose.resource.OfferActionResource;
import com.heymoose.resource.OfferGrantResource;
import com.heymoose.resource.OfferResource;
import com.heymoose.resource.OfferStatsResource;
import com.heymoose.resource.ProductResource;
import com.heymoose.resource.PublicDataResource;
import com.heymoose.resource.RegionResource;
import com.heymoose.resource.RobokassaResource;
import com.heymoose.resource.SettingResource;
import com.heymoose.resource.SiteResource;
import com.heymoose.resource.UserResource;
import com.heymoose.resource.WithdrawalResource;
import com.heymoose.resource.UserStatsResource;
import com.heymoose.resource.api.ApiResource;

public class ResourceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(UserResource.class);
    bind(ApiResource.class);
    bind(RobokassaResource.class);
    bind(AccountResource.class);
    bind(SettingResource.class);
    bind(OfferResource.class);
    bind(SiteResource.class);
    bind(OfferGrantResource.class);
    bind(CategoryResource.class);
    bind(BannerResource.class);
    bind(OfferStatsResource.class);
    bind(OfferActionResource.class);
    bind(MlmResource.class);
    bind(ErrorInfoResource.class);
    bind(RegionResource.class);
    bind(PublicDataResource.class);
    bind(WithdrawalResource.class);
    bind(UserStatsResource.class);
    bind(ProductResource.class);
    bindInterceptor(
        Matchers.any(),
        Matchers.annotatedWith(Cacheable.class),
        new CacheInterceptor());
  }
}
