package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class PercentMoneyDivider implements MoneyDivider {

  private Tariff tariff;
  private BigDecimal price;

  public PercentMoneyDivider(Tariff tariff, BigDecimal price) {
    this.price = price;
    this.tariff = tariff;
  }

  @Override
  public BigDecimal advertiserCharge() {
    return tariff.percentOf(price);
  }

  @Override
  public BigDecimal affiliatePart() {
    return tariff.affiliatePart(advertiserCharge());
  }

  @Override
  public BigDecimal heymoosePart() {
    return tariff.heymoosePart(advertiserCharge());
  }

  @Override
  public Tariff tariff() {
    return this.tariff;
  }
}
