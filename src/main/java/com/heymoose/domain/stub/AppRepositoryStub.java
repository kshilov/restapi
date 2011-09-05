package com.heymoose.domain.stub;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;

import javax.inject.Singleton;

@Singleton
public class AppRepositoryStub extends RepositoryStub<App> implements AppRepository {
  @Override
  public App byIdAndSecret(long appId, String secret) {
    for (App app : identityMap.values())
      if (app.id.equals(appId) && secret.equals(app.secret))
        return app;
    return null;
  }
}
