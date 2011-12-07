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
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
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
import com.heymoose.domain.VideoOffer;
import com.heymoose.domain.hiber.ActionRepositoryHiber;
import com.heymoose.domain.hiber.AppRepositoryHiber;
import com.heymoose.domain.hiber.BannerSizeRepositoryHiber;
import com.heymoose.domain.hiber.OfferRepositoryHiber;
import com.heymoose.domain.hiber.OfferShowRepositoryHiber;
import com.heymoose.domain.hiber.OrderRepositoryHiber;
import com.heymoose.domain.hiber.PerformerRepositoryHiber;
import com.heymoose.domain.hiber.UserRepositoryHiber;
import com.heymoose.resource.ActionResource;
import com.heymoose.resource.Api;
import com.heymoose.resource.ApiResource;
import com.heymoose.resource.AppResource;
import com.heymoose.resource.BannerSizeResource;
import com.heymoose.resource.OfferShowResource;
import com.heymoose.resource.OrderResource;
import com.heymoose.resource.PerformerResource;
import com.heymoose.resource.UserResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Properties;

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

    bind(UserRepository.class).to(UserRepositoryHiber.class);
    bind(AppRepository.class).to(AppRepositoryHiber.class);
    bind(OrderRepository.class).to(OrderRepositoryHiber.class);
    bind(OfferRepository.class).to(OfferRepositoryHiber.class);
    bind(PerformerRepository.class).to(PerformerRepositoryHiber.class);
    bind(ActionRepository.class).to(ActionRepositoryHiber.class);
    bind(OfferShowRepository.class).to(OfferShowRepositoryHiber.class);
    bind(BannerSizeRepository.class).to(BannerSizeRepositoryHiber.class);

    bindEntities(Account.class, AccountTx.class, Action.class, App.class, Targeting.class,
        Offer.class, Order.class, Performer.class, Platform.class, User.class, OfferShow.class,
        RegularOffer.class, VideoOffer.class, BannerOffer.class, BannerSize.class);
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }


  @Provides @Named("compensation")  @Singleton
  protected BigDecimal compensation(@Named("settings") Properties settings) {
    return new BigDecimal(settings.getProperty("compensation"));
  }
}
