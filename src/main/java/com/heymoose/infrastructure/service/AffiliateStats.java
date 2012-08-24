package com.heymoose.infrastructure.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.QueryResultTransformer;
import com.heymoose.infrastructure.util.SqlLoader;

import java.math.BigInteger;

public class AffiliateStats {

  private final Repo repo;

  @Inject
  public AffiliateStats(Repo repo) {
    this.repo = repo;
  }


  public enum Ordering { CANCELED, APPROVED, RATE }

  @Transactional
  public Pair<QueryResult, Long> fraudStat(boolean activeOnly,
                                           ListFilter filter,
                                           Ordering ordering,
                                           OrderingDirection direction) {
    String sql = SqlLoader.getTemplate(
        "affiliate-fraud-stat",
        ImmutableMap.of(
            "activeOnly", activeOnly,
            "ordering", ordering,
            "direction", direction));

    QueryResult result = (QueryResult) repo.session().createSQLQuery(sql)
        .setParameter("from", filter.from().toDate())
        .setParameter("to", filter.to().toDate())
        .setParameter("offset", filter.offset())
        .setParameter("limit", filter.limit())
        .setResultTransformer(QueryResultTransformer.INSTANCE)
        .list();

    Long count = ((BigInteger) repo.session().createSQLQuery(SqlLoader.countSql(sql))
        .setParameter("from", filter.from().toDate())
        .setParameter("to", filter.to().toDate())
        .uniqueResult()).longValue();

    return Pair.of(result, count);
  }
}
