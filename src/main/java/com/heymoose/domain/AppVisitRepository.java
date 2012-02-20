package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface AppVisitRepository extends Repository<AppVisit> {
  AppVisit byVisitorAppAnd(Performer visitor, App app);
}
