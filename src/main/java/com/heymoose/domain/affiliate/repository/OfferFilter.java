package com.heymoose.domain.affiliate.repository;

import com.heymoose.domain.affiliate.PayMethod;

import java.util.List;

public final class OfferFilter {

  private Long affiliateId;
  private Long advertiserId;
  private Boolean approved;
  private Boolean active;
  private Boolean launched;
  private Boolean showcase;
  private List<Long> regionIdList;
  private List<Long> categoryIdList;
  private PayMethod payMethod;

  public OfferFilter() {

  }

  public OfferFilter setAffiliateId(Long affiliateId) {
    this.affiliateId = affiliateId;
    return this;
  }

  public OfferFilter setAdvertiserId(Long advertiserId) {
    this.advertiserId = advertiserId;
    return this;
  }

  public OfferFilter setApproved(Boolean approved) {
    this.approved = approved;
    return this;
  }

  public OfferFilter setActive(Boolean active) {
    this.active = active;
    return this;
  }

  public OfferFilter setLaunched(Boolean launched) {
    this.launched = launched;
    return this;
  }

  public OfferFilter setShowcase(Boolean showcase) {
    this.showcase = showcase;
    return this;
  }

  public OfferFilter setRegionList(List<Long> regionList) {
    this.regionIdList = regionList;
    return this;
  }

  public OfferFilter setCategoryList(List<Long> categoryList) {
    this.categoryIdList = categoryList;
    return this;
  }

  public OfferFilter setPayMethod(PayMethod m) {
    this.payMethod = m;
    return this;
  }

  public Long affiliateId() {
    return affiliateId;
  }

  public Long advertiserId() {
    return advertiserId;
  }

  public Boolean approved() {
    return approved;
  }

  public Boolean active() {
    return active;
  }

  public Boolean launched() {
    return launched;
  }

  public Boolean showcase() {
    return showcase;
  }

  public List<Long> regionIdList() {
    return regionIdList;
  }

  public List<Long> categoryIdList() {
    return this.categoryIdList;
  }

  public PayMethod payMethod() {
    return this.payMethod;
  }
}
