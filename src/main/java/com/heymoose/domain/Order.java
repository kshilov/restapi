package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "offer_order")
public class Order extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order-seq")
  @SequenceGenerator(name = "order-seq", sequenceName = "offer_order_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  private Account account;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @Basic
  private BigDecimal cpa;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Basic(optional = false)
  private boolean approved;

  @Basic(optional = false)
  private boolean deleted;

  protected Order() {}

  public Order(Offer offer, BigDecimal cpa, User user, DateTime creationTime) {
    checkNotNull(offer, cpa, creationTime);
    if (cpa.signum() != 1)
      throw new IllegalArgumentException("Cpa must be positive");
    this.offer = offer;
    this.cpa = cpa;
    this.user = user;
    this.creationTime = creationTime;
    this.account = new Account();
  }

  public boolean deleted() {
    return deleted;
  }

  public User customer() {
    return user;
  }

  public Account account() {
    return account;
  }

  public void approve() {
    if (deleted)
      throw new IllegalStateException("Order was deleted");
    approved = true;
  }

  public boolean approved() {
    return approved;
  }

  public void delete() {
    deleted = true;
  }

  public BigDecimal cpa() {
    return cpa;
  }

  public Offer offer() {
    return offer;
  }
}
