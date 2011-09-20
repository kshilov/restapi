package com.heymoose.domain.hiber;

import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryHiber extends RepositoryHiber<User> implements UserRepository {
  
  @Inject
  public UserRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public User byEmail(String email) {
    return (User) hiber()
        .createQuery("from User where email = :email")
        .setParameter("email", email)
        .uniqueResult();
  }

  @Override
  protected Class<User> getEntityClass() {
    return User.class;
  }
}
