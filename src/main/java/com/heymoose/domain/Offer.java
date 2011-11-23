package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.UnsupportedEncodingException;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "offer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Offer extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-seq")
  @SequenceGenerator(name = "offer-seq", sequenceName = "offer_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @Basic(optional = false)
  private String title;

  @Basic(optional = false)
  private String url;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "type", nullable = false, insertable = false, updatable = false)
  private Type type;

  @org.hibernate.annotations.Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "offer")
  private Order order;

  @Column(name = "auto_approve", nullable = false)
  private boolean autoApprove;

  @Basic(optional = false)
  private boolean reentrant;

  public static enum Type {
    REGULAR, // 0
    BANNER, // 1
    VIDEO // 2
  }

  protected Offer() {}

  public Offer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant) {
    checkNotNull(title, url, creationTime);
    this.title = title;
    this.url = url;
    this.autoApprove = autoApprove;
    this.type = Type.REGULAR;
    this.creationTime = creationTime;
    this.reentrant = reentrant;
  }

  public Order order() {
    return order;
  }

  public String title() {
    return title;
  }

  public String url() {
    return url;
  }

  public DateTime creationTime() {
    return creationTime;
  }

  public boolean autoApprove() {
    return autoApprove;
  }

  public boolean reentrant() {
    return reentrant;
  }

  public Type type() {
    return type;
  }
}
