package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;

public class AffiliateStats {

  private final Repo repo;

  @Inject
  public AffiliateStats(Repo repo) {
    this.repo = repo;
  }


  public enum Ordering { CANCELED, APPROVED, NOT_CONFIRMED, RATE }

  @Transactional
  public Pair<QueryResult, Long> fraudStat(boolean activeOnly,
                                           DataFilter<Ordering> filter) {
    return SqlLoader.templateQuery("affiliate-fraud-stat", repo.session())
        .addTemplateParam("activeOnly", activeOnly)
        .addTemplateParam("ordering", filter.ordering())
        .addTemplateParam("direction", filter.direction())
        .addQueryParam("from", filter.from())
        .addQueryParam("to", filter.to())
        .executeAndCount(filter.offset(), filter.limit());
  }
}
