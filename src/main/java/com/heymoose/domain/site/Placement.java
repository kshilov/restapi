package com.heymoose.domain.site;

import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.base.Moderatable;
import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.offer.Offer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "placement")
public final class Placement extends ModifiableEntity implements Moderatable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "placement-seq")
  @SequenceGenerator(name = "placement-seq", sequenceName = "placement_seq", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @ManyToOne
  @JoinColumn(name = "site_id")
  private Site site;

  @Column(name = "admin_state", nullable = false)
  @Enumerated(EnumType.STRING)
  private AdminState adminState = AdminState.APPROVED;

  @Column(name = "admin_comment")
  private String adminComment;

  @Column(name = "back_url")
  private String backUrl;

  @Column(name = "postback_url")
  private String postbackUrl;

  public Placement() {
  }

  @Override
  public Long id() {
    return this.id;
  }

  public Placement setOffer(Offer offer) {
    this.offer = offer;
    return this;
  }

  public Placement setSite(Site site) {
    this.site = site;
    return this;
  }

  public Site site() {
    return this.site;
  }

  public boolean approvedByAdmin() {
    return this.adminState == AdminState.APPROVED;
  }

  public String backUrl() {
    return this.backUrl;
  }

  public String postBackUrl() {
    return this.postbackUrl;
  }

  public Placement setBackUrl(String backUrl) {
    this.backUrl = backUrl;
    return this;
  }

  public Placement setPostbackUrl(String postbackUrl) {
    this.postbackUrl = postbackUrl;
    return this;
  }

  public Placement setAdminState(AdminState state) {
    this.adminState = state;
    return this;
  }

  public AdminState adminState() {
    return this.adminState;
  }

  public Offer offer() {
    return this.offer;
  }

  public Placement setAdminComment(String adminComment) {
    this.adminComment = adminComment;
    return this;
  }

  public String adminComment() {
    return this.adminComment;
  }
}
