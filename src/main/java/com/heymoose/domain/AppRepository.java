package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface AppRepository extends Repository<App> {
  App byIdAndSecret(long appId, String secret);
}
