package com.heymoose.domain.grant;

import java.net.URI;

import com.heymoose.domain.user.User;
import com.heymoose.domain.base.BaseEntity;
import com.heymoose.domain.offer.Offer;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "offer_grant")
public class OfferGrant extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-grant-seq")
  @SequenceGenerator(name = "offer-grant-seq", sequenceName = "offer_grant_seq", allocationSize = 1)
  private Long id;

  @Column(name = "offer_id", nullable = false)
  private Long offerId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @Column(name = "aff_id", nullable = false)
  private Long affiliateId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @Column(name = "back_url")
  private String backUrl;

  @Column(name = "postback_url")
  private String postbackUrl;

  @Basic(optional = true)
  private String message;

  @Enumerated(EnumType.STRING)
  private OfferGrantState state;

  @Basic
  private Boolean blocked;

  @Column(name = "reject_reason")
  private String rejectReason;

  @Column(name = "block_reason")
  private String blockReason;

  @Override
  public Long id() {
    return id;
  }
  
  protected OfferGrant() { }
  
  public OfferGrant(Long offerId, Long affiliateId, String message) {
    checkNotNull(offerId, affiliateId);
    this.offerId = offerId;
    this.affiliateId = affiliateId;
    this.message = message;
    this.state = OfferGrantState.APPROVED;
    this.blocked = false;
  }
  
  public Long offerId() {
    return offerId;
  }
  
  public Offer offer() {
    return offer;
  }
  
  public Long affiliateId() {
    return affiliateId;
  }
  
  public User affiliate() {
    return affiliate;
  }
  
  public String backUrl() {
    return backUrl;
  }
  
  public void setBackUrl(URI backUrl) {
    this.backUrl = backUrl.toString();
  }
  
  public String postBackUrl() {
    return postbackUrl;
  }
  
  public void setPostbackUrl(URI postbackUrl) {
    this.postbackUrl = postbackUrl.toString();
  }
  
  public String message() {
    return message;
  }

  public boolean offerIsVisible() {
    return state == OfferGrantState.APPROVED && !blocked;
  }

  public void approve() {
    this.state = OfferGrantState.APPROVED;
  }

  public void reject(String reason) {
    this.state = OfferGrantState.REJECTED;
    this.rejectReason = reason;
  }

  public void block(String reason) {
    this.blocked = true;
    this.blockReason = reason;
  }

  public void unblock() {
    this.blocked = false;
  }

  public OfferGrantState state() {
    return state;
  }

  public boolean blocked() {
    return blocked;
  }

  public String rejectReason() {
    return rejectReason;
  }

  public String blockReason() {
    return blockReason;
  }
}
