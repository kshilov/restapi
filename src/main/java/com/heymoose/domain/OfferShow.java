package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "offer_show")
public class OfferShow extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-show-seq")
  @SequenceGenerator(name = "offer-show-seq", sequenceName = "offer_show_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "app_id")
  private App app;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "performer_id")
  private Performer performer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "show_time", nullable = true)
  private DateTime showTime;

  @Override
  public Long id() {
    return id;
  }

  protected OfferShow() {}

  public OfferShow(Offer offer, App app, Performer performer) {
    checkNotNull(offer, app, performer);
    this.app = app;
    this.performer = performer;
    this.offer = offer;
    showTime = DateTime.now();
  }

  public App app() {
    return app;
  }

  public Performer performer() {
    return performer;
  }

  public Offer offer() {
    return offer;
  }

  public DateTime showTime() {
    return showTime;
  }
}
