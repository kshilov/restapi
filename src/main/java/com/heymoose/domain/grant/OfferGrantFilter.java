package com.heymoose.domain.grant;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;

import java.util.List;

public final class OfferGrantFilter {
  private Long affiliateId;
  private Long offerId;
  private Boolean active;
  private Boolean blocked;
  private Boolean moderation;
  private OfferGrantState state;
  private List<String> regionList = ImmutableList.of();
  private List<Long> categoryIdList = ImmutableList.of();
  private PayMethod payMethod;
  private CpaPolicy cpaPolicy;
  private boolean onlyExclusiveOffers;
  private boolean productOffersOnly;
  private boolean activeOffersOnly;

  public OfferGrantFilter() { }

  public Long affiliateId() {
    return affiliateId;
  }

  public OfferGrantFilter setAffiliateId(Long affiliateId) {
    this.affiliateId = affiliateId;
    return this;
  }

  public Long offerId() {
    return offerId;
  }

  public OfferGrantFilter setOfferId(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  public Boolean active() {
    return active;
  }

  public OfferGrantFilter setActive(Boolean active) {
    this.active = active;
    return this;
  }

  public OfferGrantFilter setActiveOffersOnly(boolean activeOffersOnly) {
    this.activeOffersOnly = activeOffersOnly;
    return this;
  }

  public boolean activeOffersOnly() {
    return this.activeOffersOnly;
  }

  public Boolean moderation() {
    return moderation;
  }

  public OfferGrantFilter setModeration(Boolean moderation) {
    this.moderation = moderation;
    return this;
  }

  public OfferGrantState state() {
    return state;
  }

  public OfferGrantFilter setState(OfferGrantState state) {
    this.state = state;
    return this;
  }

  public Boolean blocked() {
    return blocked;
  }

  public OfferGrantFilter setBlocked(Boolean blocked) {
    this.blocked = blocked;
    return this;
  }

  public OfferGrantFilter setRegionList(List<String> regionList) {
    this.regionList = regionList;
    return this;
  }

  public OfferGrantFilter setCategoryList(List<Long> categoryList) {
    this.categoryIdList = categoryList;
    return this;
  }

  public OfferGrantFilter setPayMethod(PayMethod m) {
    this.payMethod = m;
    return this;
  }

  public OfferGrantFilter setCpaPolicy(CpaPolicy cpa) {
    this.cpaPolicy = cpa;
    return this;
  }

  public List<String> regionList() {
    return regionList;
  }

  public List<Long> categoryIdList() {
    return this.categoryIdList;
  }

  public PayMethod payMethod() {
    return this.payMethod;
  }

  public CpaPolicy cpaPolicy() {
    return this.cpaPolicy;
  }

  public boolean exclusiveOnly() {
    return onlyExclusiveOffers;
  }

  public OfferGrantFilter setExclusiveOnly(boolean only) {
    this.onlyExclusiveOffers = only;
    return this;
  }

  public OfferGrantFilter setProductOffersOnly(boolean onlyProductOffers) {
    this.productOffersOnly = onlyProductOffers;
    return this;
  }

  public boolean productOffersOnly() {
    return this.productOffersOnly;
  }
}
