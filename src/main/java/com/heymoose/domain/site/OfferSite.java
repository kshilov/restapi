package com.heymoose.domain.site;

import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.offer.Offer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "offer_site")
public final class OfferSite extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-site-seq")
  @SequenceGenerator(name = "offer-site-seq", sequenceName = "offer_site_seq", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @ManyToOne
  @JoinColumn(name = "site_id")
  private Site site;

  @Column(name = "approved")
  private boolean approvedByAdmin = false;

  @Column(name = "back_url")
  private String backUrl;

  @Column(name = "postback_url")
  private String postbackUrl;


  @Override
  public Long id() {
    return this.id;
  }

  public OfferSite setOffer(Offer offer) {
    this.offer = offer;
    return this;
  }

  public OfferSite setSite(Site site) {
    this.site = site;
    return this;
  }

  public OfferSite adminApprove() {
    this.approvedByAdmin = true;
    return this;
  }

  public Site site() {
    return this.site;
  }

  public boolean approvedByAdmin() {
    return this.approvedByAdmin;
  }

  public String backUrl() {
    return this.backUrl;
  }

  public String postBackUrl() {
    return this.postbackUrl;
  }

  public OfferSite setBackUrl(String backUrl) {
    this.backUrl = backUrl;
    return this;
  }

  public OfferSite setPostbackUrl(String postbackUrl) {
    this.postbackUrl = postbackUrl;
    return this;
  }
}
