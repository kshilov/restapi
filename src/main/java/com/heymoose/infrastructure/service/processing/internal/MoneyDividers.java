package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class MoneyDividers {

  private MoneyDividers() { }

  public static FixMoneyDivider fix(Tariff tariff) {
    return new FixMoneyDivider(tariff);
  }

  public static PercentMoneyDivider percent(Tariff tariff, BigDecimal amount) {
    return new PercentMoneyDivider(tariff, amount);
  }

  public static MoneyDivider doubleFix(Tariff tariff, boolean actionExisted) {
    return new DoubleFixMoneyDivider(tariff, actionExisted);
  }
}
