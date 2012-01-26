package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.hibernate.Transactional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@Singleton
public class AppRepositoryHiber extends RepositoryHiber<App> implements AppRepository {

  @Inject
  public AppRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Set<App> all() {
    return Sets.newHashSet(hiber().createQuery("from App where deleted = false order by creationTime desc").list());
  }

  @Override
  @Transactional
  public App byId(long id) {
    return (App) hiber().createQuery("from App where id = :id and deleted = false")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  protected Class<App> getEntityClass() {
    return App.class;
  }

  @Override
  public App anyById(long appId) {
    return super.byId(appId);
  }

  @Override
  public Iterable<App> list(int offset, int limit, Long userId, boolean withDeleted) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (userId != null)
      criteria.add(Restrictions.eq("user.id", userId));
    
    if (!withDeleted)
      criteria.add(Restrictions.eq("deleted", false));
    
    return criteria
        .addOrder(Order.desc("creationTime"))
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
  
  @Override
  public long count(Long userId, boolean withDeleted) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (userId != null)
      criteria.add(Restrictions.eq("user.id", userId));
    
    if (!withDeleted)
      criteria.add(Restrictions.eq("deleted", false));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }
}
