package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;

import java.math.BigDecimal;

interface InfoProvider {

  BigDecimal advertiserCharge();
  String transactionId();
  OfferStat offerStat(OfferStat source);
  OfferAction offerAction(OfferStat source);
  Token token();
  BaseOffer offer();
}
