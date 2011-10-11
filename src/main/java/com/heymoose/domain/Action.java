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
import javax.persistence.UniqueConstraint;

import java.math.BigDecimal;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(
    name = "action"
)
public class Action extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action-seq")
  @SequenceGenerator(name = "action-seq", sequenceName = "action_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "performer_id", nullable = false)
  private Performer performer;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "offer_id", nullable = false)
  private Offer offer;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "approve_time", nullable = true)
  private DateTime approveTime;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "reservation", nullable = false)
  private AccountTx reservation;

  @Basic(optional = false)
  private boolean done;

  @Basic(optional = false)
  private boolean deleted;

  protected Action() {}

  public Action(Offer offer, Performer performer) {
    checkNotNull(offer, performer);
    this.offer = offer;
    this.performer = performer;
    DateTime now = DateTime.now();
    this.creationTime = now;
    if (offer.autoApprove()) {
      done = true;
      approveTime = now;
    }
    Order order = offer.order();
    this.reservation = order.account().subtractFromBalance(order.cpa(), "Reservation");
  }

  public boolean done() {
    return done;
  }

  public boolean deleted() {
    return deleted;
  }

  public Performer performer() {
    return performer;
  }

  public AccountTx reservation() {
    return reservation;
  }

  public Offer offer() {
    return offer;
  }

  public void approve(BigDecimal compensation) {
    if (deleted)
      throw new IllegalStateException("Action was deleted");
    if (done)
      throw new IllegalStateException("Already done");
    done = true;
    approveTime = DateTime.now();
    performer.app().owner().developerAccount().addToBalance(reservation.diff().negate().multiply(compensation), "Action approved");
  }

  public void delete() {
    if (done)
      throw new IllegalStateException("Action is done");
    deleted = true;
  }

  public DateTime creationTime() {
    return creationTime;
  }

  public DateTime approveTime() {
    return approveTime;
  }
}
