package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.tariff.Tariff;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;

public class Tariffs {

  private final Repo repo;

  @Inject
  public Tariffs(Repo repo) {
    this.repo = repo;
  }

  public Tariff createIfNotExists(CpaPolicy policy, BigDecimal value) {
    Tariff tariff = Tariff.forValue(policy, value);
    Tariff exists = findIdentical(tariff);
    if (exists != null) return exists;
    repo.put(tariff);
    return tariff;
  }

  private Tariff findIdentical(Tariff tariff) {
    return (Tariff) repo.session().createCriteria(Tariff.class)
        .add(eqOrIsNull("cpaPolicy", tariff.cpaPolicy()))
        .add(eqOrIsNull("cost", tariff.cost()))
        .add(eqOrIsNull("percent", tariff.percent()))
        .add(eqOrIsNull("firstActionCost", tariff.firstActionCost()))
        .add(eqOrIsNull("otherActionCost", tariff.otherActionCost()))
        .add(eqOrIsNull("fee", tariff.fee()))
        .add(eqOrIsNull("feeType", tariff.feeType())).uniqueResult();

  }

  private Criterion eqOrIsNull(String name, Object value) {
    if (value == null) return Restrictions.isNull(name);
    return Restrictions.eq(name, value);
  }
}
