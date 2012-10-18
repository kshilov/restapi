package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class FixMoneyDivider implements MoneyDivider {

  private Tariff tariff;

  public FixMoneyDivider(Tariff tariff) {
    this.tariff = tariff;
  }

  @Override
  public BigDecimal advertiserCharge() {
    return tariff.cost();
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
    return tariff;
  }
}
