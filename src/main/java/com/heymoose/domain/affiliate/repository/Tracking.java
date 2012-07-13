package com.heymoose.domain.affiliate.repository;

import com.google.common.base.Optional;
import com.heymoose.domain.affiliate.BaseOffer;
import com.heymoose.domain.affiliate.OfferAction;
import com.heymoose.domain.affiliate.OfferStat;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.domain.affiliate.Token;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public interface Tracking {
  OfferStat trackShow(
      @Nullable Long bannerId, long offerId, long master, long affId, @Nullable String sourceId, Subs subs);

  String trackClick(
      @Nullable Long bannerId, long offerId, long master, long affId, @Nullable String sourceId, Subs subs,
      Map<String, String> affParams, @Nullable String referer, @Nullable String keywords);

  List<OfferAction> trackConversion(
      Token token, String transactionId, Map<BaseOffer, Optional<Double>> offers);
}
