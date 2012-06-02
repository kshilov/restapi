package com.heymoose.domain.affiliate;

import com.google.common.base.Optional;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public interface Tracking {
  OfferStat trackShow(@Nullable Long bannerId, long offerId, long master, long affId,
                      @Nullable String subId, @Nullable String sourceId);

  String trackClick(@Nullable Long bannerId, long offerId, long master, long affId,
                    @Nullable String subId, @Nullable String sourceId, Map<String, String> affParams);

  List<OfferAction> trackConversion(Token token, String transactionId, Map<BaseOffer, Optional<Double>> offers);
}
