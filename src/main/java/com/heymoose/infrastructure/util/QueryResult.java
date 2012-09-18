package com.heymoose.infrastructure.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QueryResult extends AbstractList<Map<String, Object>> {

  private static final QueryResult EMPTY =
      new QueryResult(Collections.<Map<String,Object>>emptyList());

  public static QueryResult empty() {
    return EMPTY;
  }

  private final List<Map<String, Object>> list;

  public QueryResult(List<Map<String, Object>> list) {
    this.list = list;
  }

  @Override
  public Map<String, Object> get(int i) {
    return list.get(i);
  }

  @Override
  public int size() {
    return list.size();
  }
}
