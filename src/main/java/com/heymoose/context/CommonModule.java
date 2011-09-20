package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.hiber.ActionRepositoryHiber;
import com.heymoose.domain.hiber.AppRepositoryHiber;
import com.heymoose.domain.hiber.OfferRepositoryHiber;
import com.heymoose.domain.hiber.OrderRepositoryHiber;
import com.heymoose.domain.hiber.PerformerRepositoryHiber;
import com.heymoose.domain.hiber.UserRepositoryHiber;
import com.heymoose.resource.ActionResource;
import com.heymoose.resource.AppResource;
import com.heymoose.resource.OfferResource;
import com.heymoose.resource.OrderResource;
import com.heymoose.resource.UserResource;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());
    bind(UserResource.class);
    bind(AppResource.class);
    bind(OrderResource.class);
    bind(OfferResource.class);
    bind(ActionResource.class);

    /*bind(UserRepository.class).to(UserRepositoryStub.class);
    bind(AppRepository.class).to(AppRepositoryStub.class);
    bind(OrderRepository.class).to(OrderRepositoryStub.class);
    bind(OfferRepository.class).to(OfferRepositoryStub.class);
    bind(PerformerRepository.class).to(PerformerRepositoryStub.class);
    bind(ActionRepository.class).to(ActionRepositoryStub.class);*/

    bind(UserRepository.class).to(UserRepositoryHiber.class);
    bind(AppRepository.class).to(AppRepositoryHiber.class);
    bind(OrderRepository.class).to(OrderRepositoryHiber.class);
    bind(OfferRepository.class).to(OfferRepositoryHiber.class);
    bind(PerformerRepository.class).to(PerformerRepositoryHiber.class);
    bind(ActionRepository.class).to(ActionRepositoryHiber.class);

    bindEntities(Account.class, AccountTx.class, Action.class, App.class,
        Offer.class, Order.class, Performer.class, Platform.class, User.class);
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }
}
