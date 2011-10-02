package com.heymoose.util;

import com.google.common.collect.Lists;

import java.util.List;

public class Paging {
  
  private Paging() {}

  public static <T> List<T> page(List<T> all, int offset, int limit) {
    List<T> page = Lists.newArrayList();
    int counter = 0;
    for (int i = 0; i < all.size(); i++) {
      if (i < offset)
        continue;
      if (counter >= limit)
        break;
      page.add(all.get(i));
      counter++;
    }
    return page;
  }
}
