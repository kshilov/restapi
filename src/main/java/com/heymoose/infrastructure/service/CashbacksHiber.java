package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;

import java.util.List;

public class CashbacksHiber implements Cashbacks {

  private final Repo repo;

  @Inject
  public CashbacksHiber(Repo repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  public List<Cashback> list() {
    return repo.allByHQL(Cashback.class, "from Cashback");
  }

  @Override
  @Transactional
  public Cashback add(Cashback cashback) {
    repo.put(cashback);
    return cashback;
  }

  @Override
  public Pair<QueryResult, Long> list(Long affId, int offset, int limit) {
    return SqlLoader.sqlQuery("cashback-list", repo.session())
        .addQueryParam("aff_id", affId)
        .executeAndCount(offset, limit);
  }

  @Override
  public boolean containTarget(String targetId) {
    return false;
  }
}
