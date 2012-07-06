package com.heymoose.util;


import com.floreysoft.jmte.Engine;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import java.nio.charset.Charset;
import java.util.Map;

public final class SqlLoader {
  private static final String FOLDER = "sql/";
  private static final String SQL_EXTENSION = ".sql";
  private static final String TEMPLATE_EXTENSION = ".jmte.sql";
  private static final ClassLoader CLASS_LOADER =
      SqlLoader.class.getClassLoader();
  private static final Engine TEMPLATE_ENGINE = new Engine();

  private static final Map<String, String> cache = Maps.newHashMap();

  public static final String get(String sqlName) {
    synchronized(cache) {
      String sql = cache.get(sqlName);
      if (sql == null) {
        sql = load(FOLDER + sqlName + SQL_EXTENSION);
        cache.put(sqlName, sql);
      }
      return sql;
    }
  }

  public static final String getTemplate(String templateName,
                                         Map<String, Object> params) {
    return TEMPLATE_ENGINE.transform(
        load(FOLDER + templateName + TEMPLATE_EXTENSION),
        params);
  }

  private static final String load(String relativePath) {
    try {
      return Resources.toString(
          CLASS_LOADER.getResource(relativePath),
          Charset.defaultCharset());
    } catch (Exception e) {
      String msg = String.format("File '%s' not found.", relativePath);
      throw new RuntimeException(msg, e);
    }
  }

}
