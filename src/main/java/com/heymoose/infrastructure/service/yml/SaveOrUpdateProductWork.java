package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.product.Product;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

final class SaveOrUpdateProductWork implements Work {

  private static Logger log =
      LoggerFactory.getLogger(SaveOrUpdateProductWork.class);

  private static final String INSERT = "insert into product " +
      "(shop_category_id, offer_id, tariff_id, name, url, original_id, price) " +
      "values (?, ?, ?, ?, ?, ?, ?)";
  private static final String UPDATE = "update product " +
      "set shop_category_id = ?, " +
      "offer_id = ?, " +
      "tariff_id = ?, " +
      "name = ?, " +
      "url = ?, " +
      "original_id = ?, " +
      "price = ? " +
      "where offer_id = ? and original_id = ?";

  private final Product product;

  SaveOrUpdateProductWork(Product product) {
    this.product = product;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    // try update
    PreparedStatement update = connection.prepareStatement(UPDATE,
        Statement.RETURN_GENERATED_KEYS);
    int i = fillStatementBase(update);
    // fill where cause of update query
    update.setLong(i++, product.offer().id());
    update.setString(i++, product.originalId());
    int rowsAffected = update.executeUpdate();
    if (rowsAffected > 0) {
      setProductIdFromStatement(update);
      log.debug("Product updated: {}", product);
      return;
    }
    PreparedStatement insert = connection.prepareStatement(INSERT,
        Statement.RETURN_GENERATED_KEYS);
    fillStatementBase(insert);
    insert.executeUpdate();
    setProductIdFromStatement(insert);
    log.debug("Product inserted: {}", product);
  }

  private int fillStatementBase(PreparedStatement statement)
      throws SQLException {
    int i = 1;
    if (product.category() == null) {
      statement.setNull(i++, Types.BIGINT);
    } else {
      statement.setLong(i++, product.category().id());
    }
    statement.setLong(i++, product.offer().id());
    if (product.tariff() != null) {
      statement.setLong(i++, product.tariff().id());
    } else {
      statement.setNull(i++, Types.BIGINT);
    }
    statement.setString(i++, product.name());
    statement.setString(i++, product.url());
    statement.setString(i++, product.originalId());
    statement.setBigDecimal(i++, product.price());
    return i;
  }

  private void setProductIdFromStatement(PreparedStatement statement)
      throws SQLException {
    ResultSet keys = statement.getGeneratedKeys();
    if (keys.next()) product.setId(keys.getLong(1));
  }
}
