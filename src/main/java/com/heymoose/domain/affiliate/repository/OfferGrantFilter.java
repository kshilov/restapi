package com.heymoose.domain.affiliate.repository;

import com.heymoose.domain.affiliate.OfferGrantState;
import com.heymoose.domain.affiliate.PayMethod;

import java.util.List;

public final class OfferGrantFilter {
  private Long affiliateId;
  private Long offerId;
  private Boolean active;
  private Boolean blocked;
  private Boolean moderation;
  private OfferGrantState state;
  private List<String> regionList;
  private List<Long> categoryIdList;
  private PayMethod payMethod;

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

  public List<String> regionList() {
    return regionList;
  }

  public List<Long> categoryIdList() {
    return this.categoryIdList;
  }

  public PayMethod payMethod() {
    return this.payMethod;
  }
}
