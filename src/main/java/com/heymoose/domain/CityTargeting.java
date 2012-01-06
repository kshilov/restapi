package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.isEmpty;
import java.util.Set;

public class CityTargeting {

  public CityTargeting(CityFilterType type, Iterable<City> cities) {
    checkArgument(type != null);
    checkArgument(cities != null);
    checkArgument(!isEmpty(cities));
    for (City city : cities)
      checkArgument(!city.disabled());
    this.type = type;
    this.cities = ImmutableSet.copyOf(cities);
  }

  public final CityFilterType type;
  public final Set<City> cities;
}
