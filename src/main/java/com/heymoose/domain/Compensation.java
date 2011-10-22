package com.heymoose.domain;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

public class Compensation {

  private Compensation() {}

  public static BigDecimal subtractCompensation(BigDecimal amount, BigDecimal compensation) {
    checkArgument(amount.signum() == 1);
    checkArgument(compensation.signum() == 1);
    checkArgument(compensation.compareTo(new BigDecimal(1.0)) == -1);
    return amount.subtract(amount.multiply(compensation));
  }
}
