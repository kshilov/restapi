package com.heymoose.infrastructure.util;

import com.google.common.collect.ImmutableMap;
import org.hibernate.transform.BasicTransformerAdapter;

public final class ImmutableMapTransformer extends
    BasicTransformerAdapter {

  public static final ImmutableMapTransformer INSTANCE =
      new ImmutableMapTransformer();

  private ImmutableMapTransformer() { }

  @Override
  public Object transformTuple(Object[] tuple, String[] aliases) {
    ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
    for (int i = 0; i < aliases.length; i++) {
      if (tuple[i] == null) continue;
      map.put(aliases[i], tuple[i]);
    }
    return map.build();
  }
}
