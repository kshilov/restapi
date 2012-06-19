package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.AdminAccount;
import com.heymoose.domain.AdminAccountNotConfirmed;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.affiliate.Banner;
import com.heymoose.domain.affiliate.BaseOffer;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.IpSegment;
import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.OfferAction;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.affiliate.OfferLoader;
import com.heymoose.domain.affiliate.OfferRepository;
import com.heymoose.domain.affiliate.OfferStat;
import com.heymoose.domain.affiliate.Site;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.SubOfferRepository;
import com.heymoose.domain.affiliate.Token;
import com.heymoose.domain.affiliate.Tracking;
import com.heymoose.domain.affiliate.TrackingImpl;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.affiliate.counter.BufferedClicks;
import com.heymoose.domain.affiliate.counter.BufferedShows;
import com.heymoose.domain.affiliate.hiber.HibernateRepo;
import com.heymoose.domain.affiliate.hiber.OfferGrantRepositoryHiber;
import com.heymoose.domain.affiliate.hiber.OfferRepositoryHiber;
import com.heymoose.domain.affiliate.hiber.SubOfferRepositoryHiber;
import com.heymoose.domain.hiber.UserRepositoryHiber;
import com.heymoose.domain.mlm.Mlm;
import com.heymoose.domain.mlm.MlmExecution;
import com.heymoose.domain.settings.Setting;
import com.heymoose.domain.settings.Settings;
import com.heymoose.resource.AccountResource;
import com.heymoose.resource.BannerResource;
import com.heymoose.resource.CategoryResource;
import com.heymoose.resource.MlmResource;
import com.heymoose.resource.OfferActionResource;
import com.heymoose.resource.OfferGrantResource;
import com.heymoose.resource.OfferResource;
import com.heymoose.resource.OfferStatsResource;
import com.heymoose.resource.RobokassaResource;
import com.heymoose.resource.SettingResource;
import com.heymoose.resource.SiteResource;
import com.heymoose.resource.UserResource;
import com.heymoose.resource.api.ApiResource;
import java.io.File;
import java.util.Properties;
import javax.inject.Named;
import javax.inject.Singleton;

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
  }
}
