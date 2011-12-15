package com.heymoose.domain;

import com.heymoose.domain.base.Repository;
import java.util.Map;

public interface CityRepository extends Repository<City> {
  public Map<String, City> byIds(Iterable<Long> ids);
}
