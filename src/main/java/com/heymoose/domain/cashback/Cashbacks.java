package com.heymoose.domain.cashback;

import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;

import java.util.List;

public interface Cashbacks {
  List<Cashback> list();
  Cashback add(Cashback cashback);
  Pair<QueryResult,Long> list(Long affId, int offset, int limit);
  boolean containTarget(String targetId);
  Pair<QueryResult,Long> listInvites(Long affId, int offset, int limit);
}
