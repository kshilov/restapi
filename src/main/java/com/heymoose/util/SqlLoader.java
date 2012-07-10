package com.heymoose.util;


import com.floreysoft.jmte.Engine;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Map;
import org.joda.time.DateTime;

public final class SqlLoader {
  private static final String FOLDER = "sql/";
  private static final String SQL_EXTENSION = ".sql";
  private static final String TEMPLATE_EXTENSION = ".jmte.sql";
  private static final ClassLoader CLASS_LOADER =
      SqlLoader.class.getClassLoader();
  private static final Engine TEMPLATE_ENGINE = new Engine();

  private static final Map<String, String> cache = Maps.newHashMap();

  public static final String get(String sqlName) {
    synchronized (cache) {
      String sql = cache.get(sqlName);
      if (sql == null) {
        sql = load(FOLDER + sqlName + SQL_EXTENSION);
        cache.put(sqlName, sql);
      }
      return sql;
    }
  }

  @SuppressWarnings("unchecked")
  public static final String getTemplate(String templateName,
                                         Map<String, ?> params) {
    return TEMPLATE_ENGINE.transform(
        load(FOLDER + templateName + TEMPLATE_EXTENSION),
        (Map<String, Object>) params);
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

  public static String countSql(String sql) {
    sql = sql.replaceFirst("select .* from ", "select count(*) from ");
    sql = sql.substring(0, sql.lastIndexOf("order by"));
    return "select count(*) from (" + sql + ") c";
  }

  public static Long extractLong(Object val) {
    if (val == null)
      return 0L;
    if (val instanceof BigInteger)
      return ((BigInteger) val).longValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).longValue();
    if (val instanceof Integer)
      return ((Integer) val).longValue();
    if (val instanceof Long)
      return (Long) val;
    throw new IllegalStateException();
  }

  public static double extractDouble(Object val) {
    if (val == null)
      return 0.0;
    if (val instanceof BigInteger)
      return ((BigInteger) val).doubleValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).doubleValue();
    throw new IllegalStateException();
  }

  public static String extractString(Object val) {
    if (val == null)
      return null;
    if (val instanceof String)
      return (String) val;
    throw new IllegalStateException();
  }

  public static DateTime extractDateTime(Object val) {
    if (val == null)
      return null;
    if (val instanceof Timestamp)
      return new DateTime(((Timestamp) val).getTime());
    throw new IllegalStateException();
  }

  public static Boolean extractBoolean(Object val) {
    if (val == null)
      return null;
    if (val instanceof Boolean)
      return (Boolean) val;
    throw new IllegalStateException();
  }

  public static BigDecimal scaledDecimal(Object object) {
    if (object == null)
      return new BigDecimal("0.00");
    BigDecimal decimal = (BigDecimal) object;
    return decimal.setScale(2, BigDecimal.ROUND_UP);
  }
}
