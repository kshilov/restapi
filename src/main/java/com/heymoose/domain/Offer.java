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
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import java.io.UnsupportedEncodingException;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "offer")
public class Offer extends IdEntity {

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
  private String description;

  @Basic(optional = false)
  private String body;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private Type type;

  @Basic(optional = false)
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

  public Offer(String title, String description, String body, String imageBase64, boolean autoApprove, DateTime creationTime) {
    checkNotNull(title, description, body, creationTime);
    this.title = title;
    this.description = description;
    this.body = body;
    this.autoApprove = autoApprove;
    this.type = Type.URL;
    this.creationTime = creationTime;
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public Order order() {
    return order;
  }

  public String title() {
    return title;
  }

  public String description() {
    return description;
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
  
  public String imageBase64() {
    try {
      return new String(image, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
