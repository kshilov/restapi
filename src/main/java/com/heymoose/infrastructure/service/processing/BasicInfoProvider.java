package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;

public abstract class BasicInfoProvider implements InfoProvider {

  private BaseOffer offer;
  private Token token;
  private String transactionId;

  public BasicInfoProvider setOffer(BaseOffer offer) {
    this.offer = offer;
    return this;
  }

  public BasicInfoProvider setToken(Token token) {
    this.token = token;
    return this;
  }

  public String transactionId() {
    return transactionId;
  }

  @Override
  public BaseOffer offer() {
    return offer;
  }

  @Override
  public OfferAction offerAction(OfferStat source) {
    return new OfferAction(token, source.affiliate(),
        this.offerStat(source), source, offer, transactionId);
  }

  @Override
  public OfferStat offerStat(OfferStat source) {
    return new OfferStat(
        source.bannerId(),
        offer.id(),
        offer.master(),
        source.affiliate().id(),
        source.sourceId(),
        source.subs(),
        source.referer(),
        source.keywords());
  }

  @Override
  public Token token() {
    return token;
  }


}
