package com.heymoose.infrastructure.util;

import org.hibernate.transform.BasicTransformerAdapter;

import java.util.List;

public final class QueryResultTransformer extends BasicTransformerAdapter {

  public static final QueryResultTransformer INSTANCE =
      new QueryResultTransformer();

  private QueryResultTransformer() { }

  @Override
  public Object transformTuple(Object[] tuple, String[] aliases) {
    return ImmutableMapTransformer.INSTANCE.transformTuple(tuple, aliases);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List transformList(List list) {
    return new QueryResult(list);
  }
}
