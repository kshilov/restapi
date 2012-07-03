package com.heymoose.util;


import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public final class SqlLoader {
  private static final String FOLDER = "sql/";
  private static final String EXTENSION = ".sql";
  private static final ClassLoader CLASS_LOADER =
      SqlLoader.class.getClassLoader();

  private static final Map<String, String> cache = Maps.newHashMap();

  public static final String get(String sqlName) {
    synchronized(cache) {
      String sql = cache.get(sqlName);
      if (sql == null) {
        sql = load(sqlName);
        cache.put(sqlName, sql);
      }
      return sql;
    }
  }

  private static final String load(String sqlName) {
    try {
      return Resources.toString(
          CLASS_LOADER.getResource(FOLDER + sqlName + EXTENSION),
          Charset.defaultCharset());
    } catch (IOException e) {

    }
    try {
      return Resources.toString(
          CLASS_LOADER.getResource(FOLDER + sqlName),
          Charset.defaultCharset());
    } catch (IOException e) {
      String msg = String.format("Sql '%s' not found.", sqlName);
      throw new RuntimeException(msg, e);
    }
  }

}
