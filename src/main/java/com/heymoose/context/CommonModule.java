package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.CategoryResource;
import com.heymoose.domain.affiliate.IpSegment;
import com.heymoose.domain.affiliate.OfferRepository;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.affiliate.ShowStat;
import com.heymoose.domain.affiliate.Site;
import com.heymoose.domain.affiliate.SiteResource;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.SubOfferRepository;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.affiliate.hiber.HibernateRepo;
import com.heymoose.domain.affiliate.hiber.OfferRepositoryHiber;
import com.heymoose.domain.affiliate.hiber.OfferGrantRepositoryHiber;
import com.heymoose.domain.affiliate.hiber.SubOfferRepositoryHiber;
import com.heymoose.domain.hiber.UserRepositoryHiber;
import com.heymoose.domain.settings.Setting;
import com.heymoose.domain.settings.Settings;
import com.heymoose.resource.AccountResource;
import com.heymoose.resource.RobokassaResource;
import com.heymoose.resource.SettingResource;
import com.heymoose.resource.UserResource;
import com.heymoose.resource.affiliate.NewOfferResource;
import com.heymoose.resource.affiliate.OfferGrantResource;
import com.heymoose.resource.api.Api;
import com.heymoose.resource.api.ApiResource;
import java.io.File;
import java.util.Properties;
import javax.inject.Named;
import javax.inject.Singleton;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());
    bind(UserResource.class);
    bind(ApiResource.class);
    bind(Api.class);
    bind(RobokassaResource.class);
    bind(AccountResource.class);
    bind(Settings.class);
    bind(SettingResource.class);
    bind(NewOfferResource.class);
    bind(SiteResource.class);
    bind(OfferGrantResource.class);
    bind(CategoryResource.class);

    bind(UserRepository.class).to(UserRepositoryHiber.class);
    bind(Repo.class).to(HibernateRepo.class);
    bind(OfferRepository.class).to(OfferRepositoryHiber.class);
    bind(SubOfferRepository.class).to(SubOfferRepositoryHiber.class);
    bind(OfferGrantRepository.class).to(OfferGrantRepositoryHiber.class);

    bindEntities(Offer.class, User.class, Banner.class, Withdraw.class, Setting.class, ShowStat.class, Site.class, BaseOffer.class, SubOffer.class, OfferGrant.class,
        Category.class, IpSegment.class, Account.class, AccountingEntry.class, AccountingTransaction.class);
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }

  @Provides @Named("robokassaPass")  @Singleton
  protected String robokassaPass(@Named("settings") Properties settings) {
    return settings.getProperty("robokassaPass");
  }

  @Provides @Named("banners-dir")  @Singleton
  protected String bannersDir(@Named("settings") Properties settings) {
    String bannerDirStr = settings.getProperty("banners.dir");
    File bannerDir = new File(bannerDirStr);
    if (!bannerDir.exists())
      bannerDir.mkdirs();
    else if (!bannerDir.isDirectory())
      throw new IllegalArgumentException("Not a directory: "+ bannerDirStr);
    return bannerDirStr;
  }

  @Provides @Singleton
  protected Ehcache cache() {
    CacheManager cacheManager = CacheManager.create();
    Ehcache cache = new Cache(
        new CacheConfiguration("ctr", 200)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .timeToLiveSeconds(30 * 60)
            .diskPersistent(false)
            .overflowToDisk(false)
            .overflowToOffHeap(false)
    );
    cacheManager.addCache(cache);
    return cache;
  }
}
