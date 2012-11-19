package com.heymoose.infrastructure.util.db;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.Pair;
import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigInteger;
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

  public Pair<QueryResult, Long> executeAndCount(int offset, int limit) {
    String sql = SqlLoader.getSql(name);
    String countSql = SqlLoader.countSql(sql);
    Query query = session.createSQLQuery(sql)
        .setResultTransformer(QueryResultTransformer.INSTANCE)
        .setFirstResult(offset)
        .setMaxResults(limit);
    Query countQuery = session.createSQLQuery(countSql);
    for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
      countQuery.setParameter(param.getKey(), param.getValue());
    }
    Long resultCount = ((BigInteger) countQuery.uniqueResult()).longValue();
    return Pair.of((QueryResult) query.list(), resultCount);
  }
}
