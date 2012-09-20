package com.heymoose.infrastructure.util;


import com.floreysoft.jmte.Engine;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Map;

public final class SqlLoader {

  public static class TemplateQuery {
    private final Session session;
    private final String name;
    private final ImmutableMap.Builder<String, Object> queryParamMap =
        ImmutableMap.builder();
    private final ImmutableMap.Builder<String, Object> templateParamMap =
        ImmutableMap.builder();

    private TemplateQuery(String name, Session session) {
      this.session = session;
      this.name = name;
    }

    public TemplateQuery addQueryParam(String name, Object value) {
      queryParamMap.put(name, value);
      return this;
    }

    public TemplateQuery addTemplateParam(String name, Object value) {
      templateParamMap.put(name, value);
      return this;
    }

    public QueryResult execute() {
      String sql = getTemplate(name, templateParamMap.build());
      Query query = session.createSQLQuery(sql)
          .setResultTransformer(QueryResultTransformer.INSTANCE);
      for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
        query.setParameter(param.getKey(), param.getValue());
      }
      return (QueryResult) query.list();
    }

    public Pair<QueryResult, Long> executeAndCount(int offset, int limit) {
      String sql = getTemplate(name, templateParamMap.build());
      Query query = session.createSQLQuery(sql)
          .setFirstResult(offset)
          .setMaxResults(limit)
          .setResultTransformer(QueryResultTransformer.INSTANCE);
      Query countQuery = session.createSQLQuery(countSql(sql));
      for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
        query.setParameter(param.getKey(), param.getValue());
        countQuery.setParameter(param.getKey(), param.getValue());
      }
      return Pair.of(
          (QueryResult) query.list(),
          ((BigInteger) countQuery.uniqueResult()).longValue());
    }

  }


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
    sql = sql.replaceFirst("select .* from ", "select count(*) from ");
    sql = sql.substring(0, sql.lastIndexOf("order by"));
    return "select count(*) from (" + sql + ") c";
  }

  public static TemplateQuery templateQuery(String name,
                                                      Session session) {
    return new TemplateQuery(name, session);
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
