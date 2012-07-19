package com.heymoose.domain.topshop;

import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.offer.Offer;

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
import java.math.BigDecimal;


@Entity
@Table(name = "topshop_product")
public final class TopShopProduct extends IdEntity {

  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @Column
  private BigDecimal price;

  protected TopShopProduct() { }

  public TopShopProduct(Long id) {
    this.id = id;
  }

  public Offer offer() {
    return offer;
  }

  public BigDecimal price() {
    return price;
  }

  public TopShopProduct setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  public TopShopProduct setOffer(Offer offer) {
    this.offer = offer;
    return this;
  }

  @Override
  public Long id() {
    return id;
  }
}
