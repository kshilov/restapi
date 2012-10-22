package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class YmlBasedRater implements ProductRater {

  @Override
  public Tariff rate(Product product) throws NoInfoException {
    Tariff tariff = Tariff.forProduct(product);
    Iterable<ProductAttribute> paramList = product.attributeList("param");
    for (ProductAttribute attr : paramList) {
      if (attr.getExtraInfo().get("name").equals("hm_value")) {
        String cpaPolicyString = attr.getExtraInfo().get("unit").toUpperCase();
        CpaPolicy cpaPolicy = CpaPolicy.valueOf(cpaPolicyString);
        BigDecimal value = new BigDecimal(attr.value());
        tariff.setValue(cpaPolicy, value);
      }
      if (attr.getExtraInfo().get("name").equals("hm_exclusive")) {
        product.setExclusive(Boolean.valueOf(attr.value()));
      }
    }
    if (tariff.cpaPolicy() == null)
      throw new NoInfoException("No price info for product: " + product);
    return tariff;
  }
}
