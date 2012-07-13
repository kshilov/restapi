package com.heymoose.domain.service;

import com.google.common.base.Optional;
import com.heymoose.domain.model.Token;
import com.heymoose.domain.model.action.OfferAction;
import com.heymoose.domain.model.statistics.OfferStat;
import com.heymoose.domain.model.offer.BaseOffer;
import com.heymoose.domain.model.offer.Subs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface Tracking {
  OfferStat trackShow(
      @Nullable Long bannerId, long offerId, long master, long affId, @Nullable String sourceId, Subs subs);

  String trackClick(
      @Nullable Long bannerId, long offerId, long master, long affId, @Nullable String sourceId, Subs subs,
      Map<String, String> affParams, @Nullable String referer, @Nullable String keywords);

  List<OfferAction> trackConversion(
      Token token, String transactionId, Map<BaseOffer, Optional<Double>> offers);
}
