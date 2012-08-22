package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.QueryResultTransformer;
import com.heymoose.infrastructure.util.SqlLoader;


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
}
