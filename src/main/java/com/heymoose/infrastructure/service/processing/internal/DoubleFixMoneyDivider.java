package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class DoubleFixMoneyDivider implements MoneyDivider {

  private Tariff tariff;
  private boolean actionExisted;

  public DoubleFixMoneyDivider(Tariff tariff, boolean actionExisted) {
    this.tariff = tariff;
    this.actionExisted = actionExisted;
  }

  @Override
  public BigDecimal advertiserCharge() {
    if (actionExisted) return tariff().otherActionCost();
    return tariff().firstActionCost();
  }

  @Override
  public BigDecimal affiliatePart() {
    return tariff().affiliatePart(advertiserCharge());
  }

  @Override
  public BigDecimal heymoosePart() {
    return tariff().heymoosePart(advertiserCharge());
  }

  @Override
  public Tariff tariff() {
    return tariff;
  }
}
