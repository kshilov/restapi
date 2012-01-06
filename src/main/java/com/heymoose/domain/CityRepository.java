package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface CityRepository extends Repository<City> {
  Iterable<City> all(boolean activeOnly);
}
