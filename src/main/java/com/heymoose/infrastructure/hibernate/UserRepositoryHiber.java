package com.heymoose.infrastructure.hibernate;

import com.heymoose.domain.model.Role;
import com.heymoose.domain.model.User;
import com.heymoose.domain.model.UserRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;

@Singleton
public class UserRepositoryHiber extends RepositoryHiber<User> implements
    UserRepository {
  
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
  public Iterable<User> list(int offset, int limit, Ordering ord, boolean asc) {
    return list(offset, limit, ord, asc, null);
  }
  
  @Override
  public Iterable<User> list(int offset, int limit, Ordering ord, boolean asc, Role role) {
    // Here is HQL query because Criteria API can't work with ElementCollections
    String hql = "from User ";
    if (role != null)
      hql += "where :role in elements(roles) ";
    
    String dir = asc ? "asc" : "desc";
    String fmtOrderBy = "order by ";
    switch (ord) {
    case ID: fmtOrderBy += "id %s"; break;
    case EMAIL: fmtOrderBy += "email %s, id %s"; break;
    case LAST_NAME: fmtOrderBy += "lastName %s, id %s"; break;
    case ORGANIZATION: fmtOrderBy += "organization %s, id %s"; break;
    case CONFIRMED: fmtOrderBy += "confirmed %s, id %s"; break;
    case REGISTER_TIME: fmtOrderBy += "registerTime %s, id %s"; break;
    case STAT_PAYMENTS: fmtOrderBy += "stat.payments %s, id %s"; break;
    }
    
    hql += String.format(fmtOrderBy, dir, dir);
    Query query = hiber().createQuery(hql);
    
    if (role != null)
      query.setParameter("role", role.ordinal());
    
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
    Query query;
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
