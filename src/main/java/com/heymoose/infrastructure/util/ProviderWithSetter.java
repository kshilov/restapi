package com.heymoose.infrastructure.util;

import com.google.inject.Provider;

public final class ProviderWithSetter<T> implements Provider<T> {

  public static <T> ProviderWithSetter<T> newInstance() {
    return new ProviderWithSetter<T>();
  }

  private T instance;
  public ProviderWithSetter set(T instance) {
    this.instance = instance;
    return this;
  }

  @Override
  public T get() {
    return instance;
  }
}
