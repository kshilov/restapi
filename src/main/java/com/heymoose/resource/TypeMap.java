package com.heymoose.resource;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public final class TypeMap extends AbstractMap<String, Object> {

  private Map<String, Object> wrappedMap;
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return wrappedMap.entrySet();
  }

  @Override
  public Object get(Object o) {
    return wrappedMap.get(o);
  }
}
