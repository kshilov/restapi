package com.heymoose.domain.hiber;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.City;
import com.heymoose.domain.CityRepository;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

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
  public Map<String, City> byIds(Iterable<Long> ids) {
    if (isEmpty(ids))
      return newHashMap();
    Map<String, City> map = newHashMap();
    List<City> cities = hiber()
        .createQuery("from City where id in :ids")
        .setParameterList("ids", newArrayList(ids))
        .list();
    for (City city : cities)
      map.put(city.name(), city);
    return map;
  }
}
