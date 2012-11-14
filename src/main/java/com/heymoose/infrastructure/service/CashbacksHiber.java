package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.infrastructure.persistence.Transactional;

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
}
