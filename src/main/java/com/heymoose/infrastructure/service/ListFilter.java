package com.heymoose.infrastructure.service;

import org.joda.time.DateTime;

public final class ListFilter {

  private DateTime from;
  private DateTime to;
  private int offset;
  private int limit;

  public DateTime from() {
    return from;
  }

  public ListFilter setFrom(DateTime from) {
    this.from = from;
    return this;
  }

  public DateTime to() {
    return to;
  }

  public ListFilter setTo(DateTime to) {
    this.to = to;
    return this;
  }

  public int offset() {
    return offset;
  }

  public ListFilter setOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public int limit() {
    return limit;
  }

  public ListFilter setLimit(int limit) {
    this.limit = limit;
    return this;
  }
}
