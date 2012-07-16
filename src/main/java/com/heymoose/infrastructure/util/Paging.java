package com.heymoose.infrastructure.util;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.WebResource;
import java.util.List;

public class Paging {
  private int offset = 0;
  private int limit = 2147483647;

  private Paging() {
  }

  public Paging(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  public int offset() {
    return offset;
  }

  public int limit() {
    return limit;
  }

  public WebResource addToWebQuery(WebResource wr) {
    return wr
        .queryParam("offset", Integer.toString(offset))
        .queryParam("limit", Integer.toString(limit));
  }

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
