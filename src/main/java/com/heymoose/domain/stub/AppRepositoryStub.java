package com.heymoose.domain.stub;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;

import javax.inject.Singleton;

@Singleton
public class AppRepositoryStub extends RepositoryStub<App> implements AppRepository {
}
