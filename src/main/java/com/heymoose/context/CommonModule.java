package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.AppStat;
import com.heymoose.domain.AppVisit;
import com.heymoose.domain.AppVisitRepository;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerRepository;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.domain.City;
import com.heymoose.domain.CityRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.OfferShow;
import com.heymoose.domain.OfferShowRepository;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.Targeting;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.UserStat;
import com.heymoose.domain.VideoOffer;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.hiber.ActionRepositoryHiber;
import com.heymoose.domain.hiber.AppRepositoryHiber;
import com.heymoose.domain.hiber.AppVisitRepositoryHiber;
import com.heymoose.domain.hiber.BannerRepositoryHiber;
import com.heymoose.domain.hiber.BannerSizeRepositoryHiber;
import com.heymoose.domain.hiber.CityRepositoryHiber;
import com.heymoose.domain.hiber.OfferRepositoryHiber;
import com.heymoose.domain.hiber.OfferShowRepositoryHiber;
import com.heymoose.domain.hiber.OrderRepositoryHiber;
import com.heymoose.domain.hiber.PerformerRepositoryHiber;
import com.heymoose.domain.hiber.UserRepositoryHiber;
import com.heymoose.domain.settings.Setting;
import com.heymoose.domain.settings.Settings;
import com.heymoose.resource.AccountResource;
import com.heymoose.resource.ActionResource;
import com.heymoose.resource.AppResource;
import com.heymoose.resource.BannerSizeResource;
import com.heymoose.resource.CityResource;
import com.heymoose.resource.MlmResource;
import com.heymoose.resource.OfferShowResource;
import com.heymoose.resource.OrderResource;
import com.heymoose.resource.PerformerResource;
import com.heymoose.resource.RobokassaResource;
import com.heymoose.resource.SettingResource;
import com.heymoose.resource.StatsResource;
import com.heymoose.resource.TaskResource;
import com.heymoose.resource.UserResource;
import com.heymoose.resource.api.Api;
import com.heymoose.resource.api.ApiResource;
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
    bind(AppResource.class);
    bind(OrderResource.class);
    bind(ActionResource.class);
    bind(PerformerResource.class);
    bind(Accounts.class);
    bind(OfferShowResource.class);
    bind(ApiResource.class);
    bind(Api.class);
    bind(BannerSizeResource.class);
    bind(CityResource.class);
    bind(StatsResource.class);
    bind(RobokassaResource.class);
    bind(AccountResource.class);
    bind(MlmResource.class);
    bind(Settings.class);
    bind(SettingResource.class);
    bind(TaskResource.class);

    bind(UserRepository.class).to(UserRepositoryHiber.class);
    bind(AppRepository.class).to(AppRepositoryHiber.class);
    bind(OrderRepository.class).to(OrderRepositoryHiber.class);
    bind(OfferRepository.class).to(OfferRepositoryHiber.class);
    bind(PerformerRepository.class).to(PerformerRepositoryHiber.class);
    bind(ActionRepository.class).to(ActionRepositoryHiber.class);
    bind(OfferShowRepository.class).to(OfferShowRepositoryHiber.class);
    bind(BannerSizeRepository.class).to(BannerSizeRepositoryHiber.class);
    bind(CityRepository.class).to(CityRepositoryHiber.class);
    bind(BannerRepository.class).to(BannerRepositoryHiber.class);
    bind(AppVisitRepository.class).to(AppVisitRepositoryHiber.class);

    bindEntities(Account.class, AccountTx.class, Action.class, App.class, Targeting.class,
        Offer.class, Order.class, Performer.class, Platform.class, User.class, OfferShow.class,
        RegularOffer.class, VideoOffer.class, BannerOffer.class, BannerSize.class, City.class,
        Banner.class, Withdraw.class, Setting.class, UserStat.class, AppStat.class, AppVisit.class);
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
