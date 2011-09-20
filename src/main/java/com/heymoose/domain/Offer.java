package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "offer")
public class Offer extends IdEntity {

  @Basic(optional = false)
  public String title;

  @Basic(optional = false)
  public String body;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  public Type type;

  @Basic
  public byte[] image;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time", nullable = false)
  public Date creationTime;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "offer")
  public Order order;

  public static enum Type {
    URL
  }
}
