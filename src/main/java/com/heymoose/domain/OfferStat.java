package com.heymoose.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "offer_stat")
public class OfferStat {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-stat-seq")
  @SequenceGenerator(name = "offer-stat-seq", sequenceName = "offer_stat_seq", allocationSize = 1)
  private Long id;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "offer_id")
  private Offer offer;
  
  @Column(name = "shows_overall")
  private Long showsOverall;
  
  @Column(name = "actions_overall")
  private Long actionsOverall;
  
  public OfferStat() {}
  
  public OfferStat(Offer offer) {
    this.offer = offer;
  }
  
  public Long id() {
    return id;
  }
  
  public Offer offer() {
    return offer;
  }
  
  public Long showsOverall() {
    return showsOverall;
  }
  
  public Long actionsOverall() {
    return actionsOverall;
  }
}
