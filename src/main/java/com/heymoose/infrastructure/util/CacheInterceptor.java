package com.heymoose.infrastructure.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public final class CacheInterceptor implements MethodInterceptor {

  private static final Logger log =
      LoggerFactory.getLogger(CacheInterceptor.class);

  private static final ConcurrentMap<String, Cache<List<Object>, Object>>
      cacheMap = Maps.newConcurrentMap();


  private static String buildCacheName(MethodInvocation invocation) {
    StringBuilder cacheNameBuilder = new StringBuilder()
        .append(invocation.getThis().getClass().getName())
        .append('.')
        .append(invocation.getMethod().getName())
        .append('(');
    for (Class<?> parameterType : invocation.getMethod().getParameterTypes()) {
      cacheNameBuilder.append(parameterType).append(' ');
    }
    cacheNameBuilder.setLength(cacheNameBuilder.length() - 1);
    cacheNameBuilder.append(')');
    return cacheNameBuilder.toString();

  }


  @Override
  public Object invoke(final MethodInvocation invocation) throws Throwable {
    Cacheable annotation = invocation.getMethod().getAnnotation(Cacheable.class);
    final String cacheName = buildCacheName(invocation);
    Cache<List<Object>, Object> cache = cacheMap.get(cacheName);

    if (cache == null) {
      Period period = Period.parse(annotation.period());
      int cachePeriod = period.toStandardSeconds().getSeconds();
      log.info("Creating cache '{}'. Expires after '{}'",
          cacheName, period);
      Cache<List<Object>, Object> newCache = CacheBuilder.newBuilder()
          .expireAfterWrite(cachePeriod, TimeUnit.SECONDS)
          .build();
      cacheMap.putIfAbsent(cacheName, newCache);
    }

    log.info("Searching value in cache.");
    cache = cacheMap.get(cacheName);
    final List<Object> argsList =
        Lists.newArrayList(invocation.getArguments());
    try {
      return cache.get(argsList, new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          try {
            log.info(
                "Value not found in cache {}, params: {}. Invoking method..",
                cacheName, argsList);
            return invocation.proceed();
          } catch (Throwable throwable) {
            if (throwable instanceof Exception) throw (Exception) throwable;
            throw new Exception(throwable);
          }
        }
      });
    } catch (UncheckedExecutionException e) {
      throw e.getCause();
    }
  }
}
