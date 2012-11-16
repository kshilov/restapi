package com.heymoose.domain.cashback;

import com.google.common.base.Objects;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.user.User;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "cashback")
public class Cashback extends IdEntity {


  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cashback-seq")
  @SequenceGenerator(name = "cashback-seq", sequenceName = "cashback_seq", allocationSize = 1)
  protected Long id;

  @Column(name ="target_id", nullable = false)
  private String targetId;

  @OneToOne
  @JoinColumn(name = "offer_action_id")
  private OfferAction action;

  @ManyToOne
  @JoinColumn(name = "aff_id")
  private User affiliate;

  @Basic
  private String referrer;

  public String targetId() {
    return targetId;
  }

  public OfferAction action() {
    return action;
  }

  public Cashback setTargetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  public Cashback setAction(OfferAction action) {
    this.action = action;
    return this;
  }

  public Cashback setAffiliate(User affiliate) {
    this.affiliate = affiliate;
    return this;
  }

  public User affiliate() {
    return this.affiliate;
  }

  @Override
  public Long id() {
    return id;
  }

  public String referrer() {
    return this.referrer;
  }

  public Cashback setReferrer(String referrer) {
    this.referrer = referrer;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(Cashback.class)
        .add("id", id)
        .add("target", targetId)
        .add("referrer", referrer)
        .add("action", action)
        .toString();
  }
}
