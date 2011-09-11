package com.heymoose.domain.stub;

import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;

import javax.inject.Singleton;

@Singleton
public class PerformerRepositoryStub extends RepositoryStub<Performer> implements PerformerRepository {
  @Override
  public Performer byAppAndExtId(long appId, String extId) {
    for (Performer performer : identityMap.values())
      if (performer.app.id.equals(appId) && performer.extId.equals(extId))
        return performer;
    return null;
  }
}
