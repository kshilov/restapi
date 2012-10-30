package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.infrastructure.util.BatchQuery;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.sql.SQLException;

final class ProductAttributeBatch
    extends BatchQuery<ProductAttribute> {

  private static final String SQL = "insert into product_attribute " +
      "(product_id, key, value, extra_info) values (?, ?, ?, ?)";

  public ProductAttributeBatch(Session session) {
    super(session, SQL);
  }


  @Override
  protected void transform(ProductAttribute item, PreparedStatement statement)
      throws SQLException {
    int i = 1;
    statement.setLong(i++, item.product().id());
    statement.setString(i++, item.key());
    statement.setString(i++, item.value());
    statement.setString(i++, item.extraInfoString());
  }
}
