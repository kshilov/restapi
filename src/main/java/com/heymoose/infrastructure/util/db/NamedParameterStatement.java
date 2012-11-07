package com.heymoose.infrastructure.util.db;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterStatement {

  private static final Logger log =
      LoggerFactory.getLogger(NamedParameterStatement.class);

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
