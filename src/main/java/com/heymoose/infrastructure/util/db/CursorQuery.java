package com.heymoose.infrastructure.util.db;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.ProviderWithSetter;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CursorQuery {

  private final Session session;
  private final String name;
  private final ImmutableMap.Builder<String, Object> queryParamMap =
      ImmutableMap.builder();
  private final ImmutableMap.Builder<String, Object> templateParamMap =
      ImmutableMap.builder();

  CursorQuery(String name, Session session) {
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
        NamedParameterStatement statement =
            NamedParameterStatement.create(sql, connection);
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
