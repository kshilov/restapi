package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.stub.AppRepositoryStub;
import com.heymoose.domain.stub.OrderRepositoryStub;
import com.heymoose.domain.stub.UserRepositoryStub;
import com.heymoose.resource.AppResource;
import com.heymoose.resource.OrderResource;
import com.heymoose.resource.UserResource;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    //install(new HibernateModule());
    bind(UserResource.class);
    bind(AppResource.class);
    bind(OrderResource.class);

    bind(UserRepository.class).to(UserRepositoryStub.class);
    bind(AppRepository.class).to(AppRepositoryStub.class);
    bind(OrderRepository.class).to(OrderRepositoryStub.class);

    //bindEntities();
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }
}
