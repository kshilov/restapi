package com.heymoose.domain.affiliate.repository;

public final class OfferFilter {

  private Long affiliateId;
  private Long advertiserId;
  private Boolean approved;
  private Boolean active;
  private Boolean launched;
  private Boolean showcase;

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

}
