package com.heymoose.util;

import javax.ws.rs.WebApplicationException;

public class WebAppUtil {

  private WebAppUtil() {}

  public static void checkNotNull(Object... args) {
    for (Object obj : args)
      if (obj == null)
        throw new WebApplicationException(400);
  }
  
  public static <T extends Enum<T>> T queryParamToEnum(String value, T deflt) {
    return stringToEnum(value.replace('-', '_').toUpperCase(), deflt);
  }
  
  public static <T extends Enum<T>> T stringToEnum(String value, T deflt) {
    try {
      return (T)T.valueOf(deflt.getClass(), value);
    }
    catch (IllegalArgumentException e) {
      return deflt;
    }
    catch (NullPointerException e) {
      return deflt;
    }
  }
}
