package com.heymoose.domain.product;

import com.heymoose.domain.tariff.Tariff;

public interface ProductRater {

  Tariff rate(Product product);

}
