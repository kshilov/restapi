package com.heymoose.resource;

import com.heymoose.infrastructure.util.SqlLoader;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public final class TypedMap extends AbstractMap<String, Object> {

  public static TypedMap wrap(Map<String, Object> map) {
    TypedMap newMap = new TypedMap();
    newMap.wrappedMap = map;
    return newMap;
  }

  private Map<String, Object> wrappedMap;
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return wrappedMap.entrySet();
  }

  @Override
  public Object get(Object o) {
    return wrappedMap.get(o);
  }

  public BigDecimal getBigDecimal(String key) {
    return SqlLoader.scaledDecimal(wrappedMap.get(key));
  }

  public Long getLong(String key) {
    return SqlLoader.extractLong(wrappedMap.get(key));
  }

  public Boolean getBoolean(String key) {
    return SqlLoader.extractBoolean(wrappedMap.get(key));
  }

  public String getString(String key) {
    if (!wrappedMap.containsKey(key)) return null;
    if (wrappedMap.get(key) == null) return null;
    return wrappedMap.get(key).toString();
  }

  public <T extends Enum<T>> T getEnumValue(String key, Class<T> cls) {
    if (wrappedMap.get(key) == null) return null;
    return Enum.valueOf(cls, wrappedMap.get(key).toString());
  }
}