package com.heymoose.infrastructure.util;

import com.google.common.collect.Lists;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class BatchQuery<T> {

  private static final Logger log = LoggerFactory.getLogger(BatchQuery.class);

  private final Session session;
  private final String sql;
  private List<T> itemList;

  public BatchQuery(Session session, String sql) {
    this.session = session;
    this.sql = sql;
    this.itemList = Lists.newArrayList();
  }

  public <X extends T> BatchQuery<T> add(X item) {
    itemList.add(item);
    return this;
  }

  public void flush() {
    if (itemList.size() == 0) return;
    log.debug("Flushing query \'{}\' itemList size: {}", sql, itemList.size());
    session.doWork(new Work() {
      @Override
      public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (T item : itemList) {
          transform(item, statement);
          statement.addBatch();
        }
        statement.executeBatch();
      }
    });
    this.itemList = Lists.newArrayList();
  }

  protected abstract void transform(T item, PreparedStatement statement)
      throws SQLException;

}
