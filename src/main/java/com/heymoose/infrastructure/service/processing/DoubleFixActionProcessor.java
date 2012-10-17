package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.statistics.OfferStat;

import java.math.BigDecimal;

@Singleton
public final class DoubleFixActionProcessor extends  BaseActionProcessor {


  @Inject
  protected DoubleFixActionProcessor(Repo repo, Accounting accounting,
                                     OfferGrantRepository offerGrants) {
    super(repo, accounting, offerGrants);
  }

  @Override
  protected BigDecimal advertiserCharge(ProcessableData data,
                                        OfferAction existedAction) {
    if (existedAction == null) return data.offer().tariff().firstActionCost();
    return data.offer().tariff().otherActionCost();
  }

  @Override
  protected void setCustomStatFields(ProcessableData data, OfferStat stat) {
    stat.incLeads();
  }
}
