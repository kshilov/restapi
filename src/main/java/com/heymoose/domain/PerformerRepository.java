package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface PerformerRepository extends Repository<Performer> {
  Performer byAppAndExtId(long appId, String extId);
}
