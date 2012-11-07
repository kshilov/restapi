package com.heymoose.infrastructure.util.db;


import com.floreysoft.jmte.Engine;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.hibernate.Session;
import org.joda.time.DateTime;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

public final class SqlLoader {

  private static final String FOLDER = "sql/";
  private static final String SQL_EXTENSION = ".sql";
  private static final String TEMPLATE_EXTENSION = ".jmte.sql";
  private static final ClassLoader CLASS_LOADER =
      SqlLoader.class.getClassLoader();
  private static final Engine TEMPLATE_ENGINE = new Engine();

  private static final Map<String, String> cache = Maps.newHashMap();

  public static String getSql(String sqlName) {
    return load(FOLDER + sqlName + SQL_EXTENSION);
  }

  @SuppressWarnings("unchecked")
  public static String getTemplate(String templateName,
                                         Map<String, ?> params) {
    return TEMPLATE_ENGINE.transform(
        load(FOLDER + templateName + TEMPLATE_EXTENSION),
        (Map<String, Object>) params);
  }

  private static String load(String relativePath) {
    synchronized (cache) {
      String script = cache.get(relativePath);
      if (script == null) {
        try {
          script = Resources.toString(
              CLASS_LOADER.getResource(relativePath),
              Charset.defaultCharset());
          cache.put(relativePath, script);
        } catch (Exception e) {
          String msg = String.format("File '%s' not found.", relativePath);
          throw new RuntimeException(msg, e);
        }
      }
      return script;
    }
  }

  public static String countSql(String sql) {
    sql = sql.substring(0, sql.lastIndexOf("order by"));
    return "select count(*) from (" + sql + ") c";
  }

  public static TemplateQuery templateQuery(String name, Session session) {
    return new TemplateQuery(name, session);
  }

  public static SqlQuery sqlQuery(String name, Session session) {
    return new SqlQuery(name, session);
  }

  public static Iterable<Map<String, Object>> toIterable(final ResultSet set) {
    final Map<Integer, String> columnMap;
    final int colCount;
    try {
      ResultSetMetaData meta = set.getMetaData();
      colCount = meta.getColumnCount();
      columnMap = Maps.newHashMapWithExpectedSize(colCount);
      for (int i = 1; i <= colCount; i++) {
        columnMap.put(i, meta.getColumnLabel(i));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    final Iterator<Map<String, Object>> iterator =
        new Iterator<Map<String, Object>>() {
      private boolean shifted;
      @Override
      public boolean hasNext() {
        if (shifted) return true;
        try {
          return shifted = set.next();
        } catch(SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Map<String, Object> next() {
        try {
          if (shifted) {
            shifted = false;
          } else {
            set.next();
          }
          Map<String, Object> map = Maps.newHashMap();
          for (int i = 1; i <= colCount; i++) {
            map.put(columnMap.get(i), set.getObject(i));
          }
          return map;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove() {
        throw new NotImplementedException();
      }
    };
    return IteratorWrapper.wrap(iterator);
  }

  public static CursorQuery cursorQuery(String name, Session session) {
    return new CursorQuery(name, session);
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

  public static Integer extractInteger(Object val) {
    if (val == null)
      return null;
    if (val instanceof Integer)
      return (Integer) val;
    throw new IllegalStateException();
  }
}
