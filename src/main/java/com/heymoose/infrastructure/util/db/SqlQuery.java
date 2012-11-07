package com.heymoose.infrastructure.util.db;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.Map;

public class SqlQuery {
  private final Session session;
  private final String name;
  private final ImmutableMap.Builder<String, Object> queryParamMap =
      ImmutableMap.builder();

  SqlQuery(String name, Session session) {
    this.session = session;
    this.name = name;
  }

  public SqlQuery addQueryParam(String name, Object value) {
    queryParamMap.put(name, value);
    return this;
  }

  public SqlQuery addQueryParamIfNotNull(String name, Object value) {
    if (value != null)
      queryParamMap.put(name, value);
    return this;
  }

  public QueryResult execute() {
    String sql = SqlLoader.getSql(name);
    Query query = session.createSQLQuery(sql)
        .setResultTransformer(QueryResultTransformer.INSTANCE);
    for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
    }
    return (QueryResult) query.list();
  }
}
