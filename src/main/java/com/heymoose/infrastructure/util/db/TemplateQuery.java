package com.heymoose.infrastructure.util.db;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.Pair;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

public class TemplateQuery {

  private static final Logger log =
      LoggerFactory.getLogger(TemplateQuery.class);
  private final Session session;
  private final String name;
  private final ImmutableMap.Builder<String, Object> queryParamMap =
      ImmutableMap.builder();
  private final ImmutableMap.Builder<String, Object> templateParamMap =
      ImmutableMap.builder();

  TemplateQuery(String name, Session session) {
    this.session = session;
    this.name = name;
  }

  public TemplateQuery addQueryParam(String name, Object value) {
    queryParamMap.put(name, value);
    return this;
  }

  public TemplateQuery addTemplateParam(String name, Object value) {
    templateParamMap.put(name, value);
    return this;
  }

  public TemplateQuery addQueryParamIfNotNull(Object nullable,
                                              String name, Object value) {
    if (nullable == null)
      return this;
    return addQueryParam(name, value);
  }

  public TemplateQuery addTemplateParamIfNotNull(Object nullable,
                                                 String name, Object value) {
    if (nullable == null)
      return this;
    return addTemplateParam(name, value);
  }

  public QueryResult execute() {
    String sql = SqlLoader.getTemplate(name, templateParamMap.build());
    Query query = session.createSQLQuery(sql)
        .setResultTransformer(QueryResultTransformer.INSTANCE);
    for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
      if (param.getValue() instanceof Collection<?>) {
        query.setParameterList(
            param.getKey(),
            (Collection<?>) param.getValue());
      } else {
        query.setParameter(param.getKey(), param.getValue());
      }
    }
    return (QueryResult) query.list();
  }

  public Pair<QueryResult, Long> executeAndCount(int offset, int limit) {
    String sql = SqlLoader.getTemplate(name, templateParamMap.build());
    Query query = session.createSQLQuery(sql)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .setResultTransformer(QueryResultTransformer.INSTANCE);
    Query countQuery = session.createSQLQuery(SqlLoader.countSql(sql));
    for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
      countQuery.setParameter(param.getKey(), param.getValue());
    }
    QueryResult resultList = (QueryResult) query.list();
    Long resultCount = ((BigInteger) countQuery.uniqueResult()).longValue();
    log.debug("Query executed successfully. List size: {}, total count: {}",
        resultList.size(), resultCount);
    return Pair.of(resultList, resultCount);
  }

}
