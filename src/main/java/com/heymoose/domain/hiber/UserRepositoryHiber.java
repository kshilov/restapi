package com.heymoose.domain.hiber;

import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.Role;

import org.hibernate.Session;
import org.hibernate.Query;

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
  public Iterable<User> list(int offset, int limit) {
    return list(offset, limit, null);
  }
  
  @Override
  public Iterable<User> list(int offset, int limit, Role role) {
    Query query = null;
    if (role != null)
      query = hiber()
        .createQuery("from User u where :role in elements(u.roles)")
        .setParameter("role", role.ordinal());
    else
      query = hiber().createQuery("from User");
    
    return query
      .setFirstResult(offset)
      .setMaxResults(limit)
      .list();
  }

  @Override
  public Iterable<User> referrals(long userId) {
    return hiber().createQuery("from User where referrer = :referrer")
        .setParameter("referrer", userId)
        .list();
  }

  @Override
  public long count() {
    return count(null);
  }
  
  @Override
  public long count(Role role) {
    Query query = null;
    if (role != null)
      query = hiber()
        .createQuery("select count(*) from User u " +
        		         "where :role in elements(u.roles)")
        .setParameter("role", role.ordinal());
    else
      query = hiber()
        .createQuery("select count (*) from User");
    
    return Long.parseLong(query
      .uniqueResult()
      .toString());
  }

  @Override
  protected Class<User> getEntityClass() {
    return User.class;
  }
}
