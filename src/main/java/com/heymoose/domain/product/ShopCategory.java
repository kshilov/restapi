package com.heymoose.domain.product;

import com.google.common.base.Objects;
import com.heymoose.domain.base.IdEntity;

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

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private ShopCategory parentCategory;

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
    Long parentId = parent() == null ? null : parent().id();
    return Objects.toStringHelper(ShopCategory.class)
        .add("id", id)
        .add("originalId", originalId)
        .add("offerId", offerId)
        .add("name", name)
        .add("parentId", parentId).toString();
  }

  public ShopCategory parent() {
    return this.parentCategory;
  }

  public ShopCategory setParent(ShopCategory category) {
    this.parentCategory = category;
    this.parentId = category.id();
    return this;
  }

  public ShopCategory setParentId(Long id) {
    this.parentId = id;
    return this;
  }

  public Long parentId() {
    return this.parentId;
  }

  public ShopCategory setId(Long id) {
    this.id = id;
    return this;
  }
}
