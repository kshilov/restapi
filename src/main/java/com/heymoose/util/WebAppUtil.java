package com.heymoose.util;

import javax.ws.rs.WebApplicationException;

public class WebAppUtil {

  private WebAppUtil() {}

  public static void checkNotNull(Object... args) {
    for (Object obj : args)
      if (obj == null)
        throw new WebApplicationException(400);
  }
}
