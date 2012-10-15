package com.heymoose.domain.request;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public final class Request {

  private List<QueryParameter> queryParameters;
  private String token;
  private Long ip;
  private String relativePath;
  private boolean processed;

  public Request() {
    queryParameters = Lists.newArrayList();
  }

  public String relativePath() {
    return this.relativePath;
  }

  public Request setRelativePath(String path) {
    this.relativePath = path;
    return this;
  }

  public Long ip() {
    return this.ip();
  }


  public Request setIp(Long ip) {
    this.ip = ip;
    return this;
  }

  public String token() {
    return this.token;
  }

  public Request setToken(String token) {
    this.token = token;
    return this;
  }


  public ImmutableMultimap<String, String> queryParams() {
    ImmutableMultimap.Builder<String, String> builder =
        ImmutableMultimap.builder();
    for (QueryParameter param : queryParameters) {
      builder.put(param.key(), param.value());
    }
    return builder.build();
  }


  public Request addQueryParam(String key, String value) {
    this.queryParameters.add(QueryParameter.with(key, value));
    return this;
  }

  public Request addQueryParamsFrom(Map<String, String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      this.addQueryParam(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public Request setProcessed(boolean processed) {
    this.processed = processed;
    return this;
  }

  public boolean processed() {
    return this.processed;
  }

  @Override
  public String toString() {
    Objects.ToStringHelper builder = Objects.toStringHelper(Request.class)
        .addValue(relativePath)
        .add("ip", ip)
        .add("processed", processed);
    return builder.toString();
  }

}
