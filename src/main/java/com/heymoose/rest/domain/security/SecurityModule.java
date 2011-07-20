package com.heymoose.rest.domain.security;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sun.jersey.api.core.HttpRequestContext;
import org.hibernate.SessionFactory;

public class SecurityModule extends AbstractModule {

  @Override
  protected void configure() {
        bindInterceptor(
            Matchers.annotatedWith(Secured.class),
            Matchers.any(),
            new SecuredInterceptor(getProvider(Key.get(Integer.class, Names.named("app")))));
  }

  @Provides
  @Named("app")
  protected Integer app(HttpRequestContext requestContext, Provider<SessionFactory> sessionFactoryProvider) {
    //Session session = sessionFactoryProvider.get().openSession();
    //session.beginTransaction();
    return  0;
  }
}
