package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.resource.Exceptions.badRequest;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.joda.time.DateTime;

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
  
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "offer")
  private OfferStat stat;

  public static enum Type {
    REGULAR, // 0
    BANNER, // 1
    VIDEO; // 2

    public static Type fromOrdinal(int ordinal) {
      for (Type type : values())
        if (type.ordinal() == ordinal)
          return type;
      throw badRequest();
    }
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
  
  public void setTitle(String title) {
    this.title = title;
  }

  public String url() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }

  public DateTime creationTime() {
    return creationTime;
  }

  public boolean autoApprove() {
    return autoApprove;
  }
  
  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public boolean reentrant() {
    return reentrant;
  }
  
  public void setReentrant(boolean reentrant) {
    this.reentrant = reentrant;
  }

  public Type type() {
    return type;
  }
  
  public OfferStat stat() {
    return stat;
  }
}
