package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class ProductMoneyDivider implements MoneyDivider {
  private MoneyDivider backingDivider;

  public ProductMoneyDivider(Product product, Offer offer, BigDecimal price) {
    Tariff tariff = offer.tariff();
    if (product != null) tariff = product.tariff();
    switch (tariff.cpaPolicy()) {
      case FIXED:
        this.backingDivider = new FixMoneyDivider(tariff);
        break;
      case PERCENT:
        this.backingDivider = new PercentMoneyDivider(tariff, price);
        break;
    }
  }

  @Override
  public BigDecimal advertiserCharge() {
    return backingDivider.advertiserCharge();
  }

  @Override
  public BigDecimal affiliatePart() {
    return backingDivider.affiliatePart();
  }

  @Override
  public BigDecimal heymoosePart() {
    return backingDivider.heymoosePart();
  }

  @Override
  public Tariff tariff() {
    return backingDivider.tariff();
  }
}
