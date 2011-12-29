package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.isEmpty;
import java.util.Set;

public class AppTargeting {

  public AppTargeting(AppFilterType type, Iterable<App> apps) {
    checkArgument(type != null);
    checkArgument(apps != null);
    checkArgument(!isEmpty(apps));
    this.type = type;
    this.apps = ImmutableSet.copyOf(apps);
  }

  public final AppFilterType type;
  public final Set<App> apps;
}
