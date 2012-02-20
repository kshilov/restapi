package com.heymoose.security;

import com.google.inject.Provider;
import com.google.inject.name.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class SecuredInterceptor implements MethodInterceptor {

  private final @Named("app") Provider<Long> appProvider;

  public SecuredInterceptor(Provider<Long> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (appProvider.get() == null)
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    return invocation.proceed();
  }
}
