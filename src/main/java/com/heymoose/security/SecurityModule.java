package com.heymoose.security;

import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.sun.jersey.api.core.HttpRequestContext;

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
  protected Long app(HttpRequestContext requestContext, AppRepository apps) throws Throwable {
    String appId = requestContext.getQueryParameters().getFirst("app_id");
    String sign = requestContext.getQueryParameters().getFirst("sig");
    if (isNullOrEmpty(appId) || isNullOrEmpty(sign))
      return null;
    App app = apps.byId(Long.parseLong(appId));
    if (app == null)
      return null;
    if (!sign.equals(Signer.sign(app.id(), app.secret())))
      return null;
    return app.id();
  }
}
