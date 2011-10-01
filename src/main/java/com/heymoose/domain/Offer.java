package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.joda.time.DateTime;

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

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "offer")
public class Offer extends IdEntity {

  @Basic(optional = false)
  private String title;

  @Basic(optional = false)
  private String body;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private Type type;

  @Basic
  private byte[] image;

  @org.hibernate.annotations.Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "offer")
  private Order order;

  @Column(name = "auto_approve", nullable = false)
  private boolean autoApprove;

  public static enum Type {
    URL
  }

  protected Offer() {}

  public Offer(String title, String body, boolean autoApprove, DateTime creationTime) {
    checkNotNull(title, body);
    this.title = title;
    this.body = body;
    this.autoApprove = autoApprove;
    this.type = Type.URL;
    this.creationTime = creationTime;
  }

  public Order order() {
    return order;
  }

  public String title() {
    return title;
  }

  public String body() {
    return body;
  }

  public DateTime creationTime() {
    return creationTime;
  }

  public boolean autoApprove() {
    return autoApprove;
  }
}
