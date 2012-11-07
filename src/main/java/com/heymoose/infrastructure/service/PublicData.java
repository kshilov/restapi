package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.QueryResultTransformer;
import com.heymoose.infrastructure.util.db.SqlLoader;

import java.math.BigInteger;


public class PublicData {

  private final Repo repo;

  @Inject
  public PublicData(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public QueryResult topWithdrawAffiliates(int limit) {
    String sql = SqlLoader.getSql("affiliate-top-withdraw");
    return (QueryResult) repo.session().createSQLQuery(sql)
        .setMaxResults(limit)
        .setResultTransformer(QueryResultTransformer.INSTANCE)
        .list();
  }

  @Transactional
  public QueryResult topConversionAffiliates(int limit) {
    String sql = SqlLoader.getSql("affiliate-top-conversion");
    return (QueryResult) repo.session().createSQLQuery(sql)
        .setMaxResults(limit)
        .setResultTransformer(QueryResultTransformer.INSTANCE)
        .list();
  }

  @Transactional
  public Long countActiveOffers() {
    String sql = SqlLoader.getSql("active-offer-count");
    return ((BigInteger) repo.session().createSQLQuery(sql)
        .uniqueResult()).longValue();
  }
}
