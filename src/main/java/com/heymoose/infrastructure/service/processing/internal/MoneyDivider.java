package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public interface MoneyDivider {

  BigDecimal advertiserCharge();
  BigDecimal affiliatePart();
  BigDecimal heymoosePart();
  Tariff tariff();
}
