package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;

public class AffiliateStats {

  private final Repo repo;

  @Inject
  public AffiliateStats(Repo repo) {
    this.repo = repo;
  }



  public enum Ordering {
    AFFILIATE_ID, AFFILIATE_EMAIL,
    CANCELED, APPROVED, NOT_CONFIRMED, RATE,
    CLICKS, ACTIONS, CONVERSION }

  public enum ReferralOrdering {
    EMAIL, AMOUNT
  }

  @Transactional
  public Pair<QueryResult, Long> fraudStat(boolean activeOnly,
                                           Long offerId,
                                           DataFilter<Ordering> filter) {
    return SqlLoader.templateQuery("affiliate-fraud-stat", repo.session())
        .addTemplateParam("activeOnly", activeOnly)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .addTemplateParam("ordering", filter.ordering())
        .addTemplateParam("direction", filter.direction())
        .addQueryParam("from", filter.from())
        .addQueryParam("to", filter.to())
        .executeAndCount(filter.offset(), filter.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> referralStat(Long affId, String source,
                                              DataFilter<ReferralOrdering> filter) {
    return SqlLoader.templateQuery("referral-stat", repo.session())
        .addQueryParam("aff_id", affId)
        .addTemplateParamIfNotNull(source, "filterBySource", true)
        .addQueryParamIfNotNull(source, "source", source)
        .addTemplateParam("ordering", filter.ordering())
        .addTemplateParam("direction", filter.direction())
        .executeAndCount(filter.offset(), filter.limit());
  }

}
