package com.heymoose.infrastructure.util;


import com.floreysoft.jmte.Engine;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlLoader {

  public static class CursorQuery {

    private final Session session;
    private final String name;
    private final ImmutableMap.Builder<String, Object> queryParamMap =
        ImmutableMap.builder();
    private final ImmutableMap.Builder<String, Object> templateParamMap =
        ImmutableMap.builder();

    private CursorQuery(String name, Session session) {
      this.session = session;
      this.name = name;
    }

    public CursorQuery addQueryParam(String name, Object value) {
      queryParamMap.put(name, value);
      return this;
    }

    public CursorQuery addTemplateParam(String name, Object value) {
      templateParamMap.put(name, value);
      return this;
    }

    public CursorQuery addQueryParamIfNotNull(Object nullable,
                                              String name, Object value) {
      if (nullable == null)
        return this;
      return addQueryParam(name, value);
    }

    public CursorQuery addTemplateParamIfNotNull(Object nullable,
                                                 String name, Object value) {
      if (nullable == null)
        return this;
      return addTemplateParam(name, value);
    }

    public Iterable<Map<String, Object>> execute() {
      final String sql = SqlLoader.getTemplate(name, templateParamMap.build());

      final ProviderWithSetter<Iterable<Map<String, Object>>> provider =
          ProviderWithSetter.newInstance();
      session.doWork(new Work() {
        @Override
        public void execute(Connection connection) throws SQLException {
          connection.setAutoCommit(false);
          NamedParameterStatement statement =
              NamedParameterStatement.create(sql, connection);
          statement.statement.setFetchSize(200);
          for (Map.Entry<String, Object> entry :
              CursorQuery.this.queryParamMap.build().entrySet()) {
            statement.setObject(entry.getKey(), entry.getValue());
          }
          ResultSet resultSet = statement.executeQuery();
          provider.set(SqlLoader.toIterable(resultSet));
        }
      });
      return provider.get();
    }
  }

  public static class NamedParameterStatement {

    public static NamedParameterStatement create(String sql, Connection con) {
      ImmutableMultimap.Builder<String, Integer> paramMap =
          ImmutableMultimap.builder();
      Pattern pattern = Pattern.compile(":\\w*");
      Matcher matcher = pattern.matcher(sql);
      int i = 1;
      while (matcher.find()) {
        paramMap.put(matcher.group().substring(1), i++);
      }
      String newSql = matcher.replaceAll("?");
      NamedParameterStatement result = new NamedParameterStatement();
      result.paramNameMap = paramMap.build();
      try {
        result.statement =  con.prepareStatement(newSql);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      log.debug("Query parsed: {}", sql);
      return result;
    }


    private Multimap<String, Integer> paramNameMap;
    private PreparedStatement statement;

    public NamedParameterStatement setLong(String name, long value)
        throws SQLException {
      for (Integer index : paramNameMap.get(name))
        statement.setLong(index, value);
      return this;
    }

    public NamedParameterStatement setString(String name, String value)
        throws SQLException {
      Preconditions.checkNotNull(value, "Use setNull for null values!");
      for (Integer index : paramNameMap.get(name))
        statement.setString(index, value);
      return this;
    }

    public NamedParameterStatement setInLong(String name,
                                             Iterable<Long> valueList)
        throws SQLException {
      Iterator<Long> iterator = valueList.iterator();
      for (Integer index : paramNameMap.get(name)) {
        statement.setLong(index, iterator.next());
      }
      return this;
    }

    public ResultSet executeQuery() throws SQLException {
      return statement.executeQuery();
    }

    public NamedParameterStatement setObject(String key, Object value)
        throws SQLException {
      if (value instanceof Iterable<?>) {
        return setInObject(key, (Iterable<?>) value);
      }
      for (Integer index : paramNameMap.get(key)) {
        statement.setObject(index, value);
      }
      return this;
    }

    private NamedParameterStatement setInObject(String key,
                                                Iterable<?> valueList)
        throws SQLException {
      Iterator<?> iterator = valueList.iterator();
      for (Integer index : paramNameMap.get(key)) {
        statement.setObject(index, iterator.next());
      }
      return this;
    }
  }

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

    public TemplateQuery addQueryParamIfNotNull(Object nullable,
                                                String name, Object value) {
      if (nullable == null)
        return this;
      return addQueryParam(name, value);
    }

    public TemplateQuery addTemplateParamIfNotNull(Object nullable,
                                                   String name, Object value) {
      if (nullable == null)
        return this;
      return addTemplateParam(name, value);
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
      QueryResult resultList = (QueryResult) query.list();
      Long resultCount = ((BigInteger) countQuery.uniqueResult()).longValue();
      log.debug("Query executed successfully. List size: {}, total count: {}",
          resultList.size(), resultCount);
      return Pair.of(resultList, resultCount);
    }

  }

  public static class SqlQuery {
    private final Session session;
    private final String name;
    private final ImmutableMap.Builder<String, Object> queryParamMap =
        ImmutableMap.builder();

    private SqlQuery(String name, Session session) {
      this.session = session;
      this.name = name;
    }

    public SqlQuery addQueryParam(String name, Object value) {
      queryParamMap.put(name, value);
      return this;
    }

    public SqlQuery addQueryParamIfNotNull(String name, Object value) {
      if (value != null)
        queryParamMap.put(name, value);
      return this;
    }

    public QueryResult execute() {
      String sql = getSql(name);
      Query query = session.createSQLQuery(sql)
          .setResultTransformer(QueryResultTransformer.INSTANCE);
      for (Map.Entry<String, ?> param : queryParamMap.build().entrySet()) {
        query.setParameter(param.getKey(), param.getValue());
      }
      return (QueryResult) query.list();
    }
  }

  private static final Logger log = LoggerFactory.getLogger(SqlLoader.class);
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
