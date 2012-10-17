package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.statistics.OfferStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public final class FixActionProcessor extends BaseActionProcessor {

  private static final Logger log =
      LoggerFactory.getLogger(FixActionProcessor.class);

  @Inject
  public FixActionProcessor(Repo repo, Accounting accounting,
                            OfferGrantRepository offerGrants) {
    super(repo, accounting, offerGrants);
  }

  @Override
  protected BigDecimal advertiserCharge(ProcessableData data,
                                        OfferAction existedAction) {
    return data.offer().cost();
  }

  @Override
  protected void setCustomStatFields(ProcessableData data, OfferStat stat) {
    stat.incLeads();
  }
}
