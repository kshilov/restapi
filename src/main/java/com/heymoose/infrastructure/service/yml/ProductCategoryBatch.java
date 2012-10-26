package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.product.ProductCategoryMapping;
import com.heymoose.infrastructure.util.BatchQuery;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.sql.SQLException;

class ProductCategoryBatch extends BatchQuery<ProductCategoryMapping> {

  private static final String SQL = "insert into product_category " +
      "(product_id, shop_category_id, is_direct) values (?, ?, ?)";
  private static final int BATCH_SIZE = 10;

  public ProductCategoryBatch(Session session) {
    super(BATCH_SIZE, session, SQL);
  }

  @Override
  protected void transform(ProductCategoryMapping item,
                           PreparedStatement statement) throws SQLException {
    int i = 1;
    statement.setLong(i++, item.product().id());
    statement.setLong(i++, item.category().id());
    statement.setBoolean(i++, item.isDirect());
  }
}
