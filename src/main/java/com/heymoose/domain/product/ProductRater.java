package com.heymoose.domain.product;

import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.service.yml.NoInfoException;

public interface ProductRater {

  /**
   * Creates a {@link Tariff} for product.
   * Sets {@link com.heymoose.domain.product.Product#exclusive()} field.
   *
   * @param product to rate
   * @return created tariff
   */
  Tariff rate(Product product) throws NoInfoException;

}
