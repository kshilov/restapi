package com.heymoose.cache;

import static com.google.common.collect.Maps.newHashMap;
import java.util.Map;

public class ThreadLocalMapStore<K, V> implements Store<K, V> {

  private final ThreadLocal<Map<K, V>> map = new ThreadLocal<Map<K, V>>() {
    @Override
    protected Map<K, V> initialValue() {
      return newHashMap();
    }
  };

  @Override
  public V get(K k) {
    return map.get().get(k);
  }

  @Override
  public void put(Map<K, V> entries) {
    map.get().putAll(entries);
  }

  @Override
  public void clear() {
    map.get().clear();
  }
}
