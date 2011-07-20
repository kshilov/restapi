package com.heymoose.rest.domain.security;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class SecuredInterceptor implements MethodInterceptor {

  private final @Named("app") Provider<Integer> appProvider;

  public SecuredInterceptor(Provider<Integer> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (appProvider.get() == null)
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    return invocation.proceed();
  }
}
