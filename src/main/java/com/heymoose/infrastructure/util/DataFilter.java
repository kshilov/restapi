package com.heymoose.infrastructure.util;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * @param <T> ordering enum
 */
public final class DataFilter<T extends Enum<?>> {

  public static <T extends Enum<?>> DataFilter<T> newInstance() {
    return new DataFilter<T>();
  }

  private int offset;
  private int limit;
  private DateTime dateFrom;
  private DateTime dateTo;
  private T ordering;
  private OrderingDirection direction;

  public int offset() {
    return offset;
  }

  public DataFilter<T> setOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public int limit() {
    return limit;
  }

  public DataFilter<T> setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public Date from() {
    return dateFrom.toDate();
  }

  public DataFilter<T> setFrom(DateTime dateFrom) {
    this.dateFrom = dateFrom;
    return this;
  }

  public DataFilter<T> setFrom(Long from) {
    this.dateFrom = new DateTime(from);
    return this;
  }

  public Date to() {
    return dateTo.toDate();
  }

  public DataFilter<T> setTo(DateTime to) {
    this.dateTo = new DateTime(to);
    return this;
  }

  public DataFilter<T> setTo(Long to) {
    this.dateTo = new DateTime(to);
    return this;
  }

  public T ordering() {
    return ordering;
  }

  public DataFilter<T> setOrdering(T ordering) {
    this.ordering = ordering;
    return this;
  }

  public OrderingDirection direction() {
    return direction;
  }

  public DataFilter<T> setDirection(OrderingDirection direction) {
    this.direction = direction;
    return this;
  }
}
