package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import org.joda.time.DateTime;

public class Sites {
  private final Repo repo;

  @Inject
  public Sites(Repo repo) {
    this.repo = repo;
  }

  public QueryResult stats(DateTime firstFromDate, DateTime firstToDate,
                           DateTime secondFromDate, DateTime secondToDate,
                           int offset, int limit) {
    return SqlLoader.sqlQuery("site-stats", repo.session())
        .addQueryParam("first_period_from", firstFromDate.toDate())
        .addQueryParam("first_period_to", firstToDate.toDate())
        .addQueryParam("second_period_from", secondFromDate.toDate())
        .addQueryParam("second_period_to", secondToDate.toDate())
        .addQueryParam("offset", offset)
        .addQueryParam("limit", limit)
        .execute();
  }
}
