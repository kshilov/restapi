package com.heymoose.domain.product;

import com.google.common.base.Objects;
import com.heymoose.domain.base.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "shop_category")
public class ShopCategory extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_category-seq")
  @SequenceGenerator(name = "shop_category-seq", sequenceName = "shop_category_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "offer_id", nullable = false)
  protected Long offerId;

  @Column(name = "original_id", nullable = false)
  protected String originalId;

  @Column(nullable = false)
  protected String name;

  @Column(name = "parent_original_id")
  protected String parentOriginalId;

  @Override
  public Long id() {
    return this.id;
  }

  public String name() {
    return name;
  }

  public ShopCategory setName(String name) {
    this.name = name;
    return this;
  }

  public String parentOriginalId() {
    return parentOriginalId;
  }

  public ShopCategory setParentOriginalId(String parentOriginalId) {
    this.parentOriginalId = parentOriginalId;
    return this;
  }

  public Long offerId() {
    return this.offerId;
  }

  public ShopCategory setOfferId(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  public String originalId() {
    return this.originalId;
  }

  public ShopCategory setOriginalId(String originalId) {
    this.originalId = originalId;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(ShopCategory.class)
        .add("id", id)
        .add("originalId", originalId)
        .add("offerId", offerId)
        .add("name", name)
        .add("parentOriginalId", parentOriginalId).toString();
  }
}
