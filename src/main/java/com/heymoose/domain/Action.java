package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(
    name = "action",
    uniqueConstraints = @UniqueConstraint(columnNames = {"performer_id", "offer_id"})
)
public class Action extends IdEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "performer_id", nullable = false)
  public Performer performer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", nullable = false)
  public Offer offer;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time", nullable = false)
  public Date creationTime;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "reservation", nullable = false)
  public AccountTx reservation;

  @Basic(optional = false)
  public boolean done;

  @Basic(optional = false)
  public boolean deleted;
}
