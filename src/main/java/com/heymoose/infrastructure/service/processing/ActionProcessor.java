package com.heymoose.infrastructure.service.processing;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.request.Request;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.service.OfferLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ActionProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(ActionProcessor.class);

  private OfferLoader offerLoader;
  private Tracking tracking;
  private Repo repo;

  @Inject
  public ActionProcessor(Tracking tracking,
                         OfferLoader offerLoader,
                         Repo repo) {
    this.offerLoader = offerLoader;
    this.tracking = tracking;
    this.repo = repo;
  }

  @Override
  public void process(Request request) {
    try {
      ImmutableMultimap<String, String> queryParams = request.queryParams();
      String sOffer = firstInMap(queryParams, "offer");
      String advId = firstInMap(queryParams, "advertiser_id");
      String transactionId = firstInMap(queryParams, "transaction_id");
      long advertiserId = Long.valueOf(advId);

      Token token = repo.byHQL(Token.class, "from Token where value = ?",
          request.token());
      if (token == null)
        throw new IllegalArgumentException("Token [" +
            request.token() + " ] not found.");
      String[] pairs = sOffer.split(",");
      ImmutableMultimap.Builder<BaseOffer, Optional<Double>> offers =
          ImmutableMultimap.builder();
      for (String pair : pairs) {
        String[] parts = pair.split(":");
        String code = parts[0];
        BaseOffer offer = offerLoader.findOffer(advertiserId, code);
        if (offer == null)
          throw new IllegalArgumentException("Offer not found, params code: " +
                  code + ", " + "advertiser_id: " + advertiserId);
        Optional<Double> price = (parts.length == 2)
            ? Optional.of(Double.parseDouble(parts[1]))
            : Optional.<Double>absent();
        offers.put(offer, price);
      }
      tracking.trackConversion(token, transactionId, offers.build());
    } finally {
      request.setProcessed(true);
    }
  }

  private String firstInMap(Multimap<String, String> map, String key) {
    try {
      return map.get(key).iterator().next();
    } catch (Exception e) {
      return null;
    }
  }
}
