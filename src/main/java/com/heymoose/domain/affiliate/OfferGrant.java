package com.heymoose.domain.affiliate;

import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.BaseEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.persistence.Entity;
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "offer_id")
  private Long offerId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @ManyToOne(optional = false)
  @JoinColumn(name = "aff_id")
  private Long affiliateId;

  @ManyToOne
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @ManyToOne()
  @JoinColumn(name = "back_url")
  private String backUrl;

  @ManyToOne()
  @JoinColumn(name = "post_back_url")
  private String postBackUrl;

  @ManyToOne(optional = false)
  @JoinColumn(name = "message")
  private String message;

  @ManyToOne(optional = false)
  @JoinColumn(name = "approved")
  private Boolean approved;

  @ManyToOne(optional = false)
  @JoinColumn(name = "active")
  private Boolean active;

  @Override
  public Long id() {
    return id;
  }
  
  public OfferGrant(Long offerId, Long affiliateId, String message) {
    checkNotNull(offerId, affiliateId, message);
    this.offerId = offerId;
    this.affiliateId = affiliateId;
    this.message = message;
    this.approved = false;
    this.active = false;
  }
  
  public Long offerId() {
    return offerId;
  }
  
  public NewOffer offer() {
    return (NewOffer) offer;
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
  
  public String postBackUrl() {
    return postBackUrl;
  }
  
  public String message() {
    return message;
  }

  public boolean offerIsVisible() {
    return approved && active;
  }
  
  public void moderateAsAdmin() {
    this.approved = true;
  }

  public void moderateAsAdvertiser() {
    this.active = true;
  }
}
