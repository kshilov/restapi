package com.heymoose.infrastructure.util.db;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.Pair;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.Map;

public final class HibernateQuery<T> {

  public static <T> HibernateQuery<T> create(String hql, Session session) {
    return new HibernateQuery<T>(hql, session);
  }

  private final String hql;
  private final Session session;
  private final ImmutableMap.Builder<Integer, Object> parameterMap =
      ImmutableMap.builder();

  public HibernateQuery(String hql, Session session) {
    this.session = session;
    this.hql = hql;
  }

  public HibernateQuery<T> setParameter(int index, Object value) {
    parameterMap.put(index, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Pair<Iterable<T>, Long> executeAndCount(int offset, int limit) {
    Query listQuery = session.createQuery(hql);
    String countHql = "select count(*) " + hql;
    int orderByIndex = countHql.lastIndexOf("order by");
    if (orderByIndex > -1) {
      countHql = countHql.substring(0, orderByIndex);
    }
    Query countQuery = session.createQuery(countHql);
    for (Map.Entry<Integer, Object> param : parameterMap.build().entrySet()) {
      listQuery.setParameter(param.getKey(), param.getValue());
      countQuery.setParameter(param.getKey(), param.getValue());
    }
    listQuery.setFirstResult(offset);
    listQuery.setMaxResults(limit);
    Iterable<T> list = listQuery.list();
    Long count = (Long) countQuery.uniqueResult();
    return Pair.of(list, count);
  }
}
