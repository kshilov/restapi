package com.heymoose.domain.hiber;

import com.heymoose.domain.City;
import com.heymoose.domain.CityRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@Singleton
public class CityRepositoryHiber extends RepositoryHiber<City> implements CityRepository {

  @Inject
  public CityRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<City> getEntityClass() {
    return City.class;
  }

  @Override
  public Iterable<City> all(boolean activeOnly) {
    Criteria criteria = hiber().createCriteria(City.class);
    if (activeOnly)
      criteria.add(Restrictions.eq("disabled", false));
    return criteria.list();
  }
}
