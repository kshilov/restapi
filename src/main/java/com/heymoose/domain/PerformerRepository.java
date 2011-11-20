package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface PerformerRepository extends Repository<Performer> {
  Performer byPlatformAndExtId(Platform platform, String extId);
  Iterable<Performer> list(int offset, int limit);
  long count();
}
