package com.heymoose.domain.request;

public final class QueryParameter {

  public static QueryParameter with(String key, String value) {
    return new QueryParameter(key, value);
  }

  private final String key;
  private final String value;

  public QueryParameter(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String key() {
    return this.key;
  }

  public String value() {
    return this.value;
  }
}

