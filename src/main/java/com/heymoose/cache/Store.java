package com.heymoose.cache;

import java.util.Map;

public interface Store<K, V> {
  V get(K k);
  void put(Map<K, V> entries);
  void clear();
}
