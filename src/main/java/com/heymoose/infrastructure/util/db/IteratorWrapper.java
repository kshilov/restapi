package com.heymoose.infrastructure.util.db;

import java.util.Iterator;

final class IteratorWrapper<T> implements Iterable<T> {
  private final Iterator<T> iterator;

  IteratorWrapper(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public Iterator<T> iterator() {
    return iterator;
  }

  public static <T> IteratorWrapper<T> wrap(Iterator<T> iterator) {
    return new IteratorWrapper<T>(iterator);
  }
}
