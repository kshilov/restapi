package com.heymoose.domain.stub;

import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;

import javax.inject.Singleton;

@Singleton
public class UserRepositoryStub extends RepositoryStub<User> implements UserRepository {
  @Override
  public User byEmail(String email) {
    for (User user : identityMap.values())
      if (email.equals(user.email))
        return  user;
    return null;
  }
}
