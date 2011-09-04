package com.heymoose.security;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.heymoose.domain.App;
import com.sun.jersey.api.core.HttpRequestContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SecurityModule extends AbstractModule {

  @Override
  protected void configure() {
        bindInterceptor(
            Matchers.annotatedWith(Secured.class),
            Matchers.any(),
            new SecuredInterceptor(getProvider(Key.get(Long.class, Names.named("app")))));
  }

  @Provides
  @RequestScoped
  @Named("app")
  protected Long app(HttpRequestContext requestContext, Provider<SessionFactory> sessionFactoryProvider) throws Throwable {
    String appId = requestContext.getQueryParameters().getFirst("app");
    String secret = requestContext.getQueryParameters().getFirst("secret");
    if (isNullOrEmpty(appId) || isNullOrEmpty(secret))
      return null;
    Session session = sessionFactoryProvider.get().openSession();
    session.beginTransaction();
    try {
      /*App app = (App) session
              .createQuery("from App where id = :id and secret = :secret")
              .setParameter("id", Integer.parseInt(appId))
              .setParameter("secret", secret)
              .uniqueResult(); */
      App app = null;
      if (app == null)
        return null;
      return app.id();
    } catch (Throwable t) {
      session.getTransaction().rollback();
      throw t;
    } finally {
      session.getTransaction().commit();
    }
  }
}
