package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
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

  public Tariff createIfNotExists(Tariff tariff) {
    Tariff exists = findIdentical(tariff);
    if (exists != null) return exists;
    repo.put(tariff);
    return tariff;
  }

  public Tariff createIfNotExists(CpaPolicy policy, BigDecimal value,
                                  Offer offer) {
    Tariff tariff = Tariff.forOffer(offer).setValue(policy, value);
    return createIfNotExists(tariff);
  }

  private Tariff findIdentical(Tariff tariff) {
    return (Tariff) repo.session().createCriteria(Tariff.class)
        .add(Restrictions.eq("offer", tariff.offer()))
        .add(Restrictions.eq("exclusive", tariff.exclusive()))
        .add(Restrictions.eq("cpaPolicy", tariff.cpaPolicy()))
        .add(eqOrIsNull("cost", tariff.cost()))
        .add(eqOrIsNull("percent", tariff.percent()))
        .add(eqOrIsNull("firstActionCost", tariff.firstActionCost()))
        .add(eqOrIsNull("otherActionCost", tariff.otherActionCost()))
        .add(Restrictions.eq("fee", tariff.fee()))
        .add(Restrictions.eq("feeType", tariff.feeType())).uniqueResult();

  }

  private Criterion eqOrIsNull(String name, Object value) {
    if (value == null) return Restrictions.isNull(name);
    return Restrictions.eq(name, value);
  }
}
