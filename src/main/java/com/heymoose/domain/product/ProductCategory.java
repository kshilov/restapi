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
@Table(name = "product_category")
public class ProductCategory extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_category-seq")
  @SequenceGenerator(name = "product_category-seq", sequenceName = "product_category_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "offer_id", nullable = false)
  protected Long offerId;

  @Column(name = "original_id", nullable = false)
  protected String originalId;

  @Column(nullable = false)
  protected String name;

  @Column(name = "parent_id")
  protected String parentId;

  @Override
  public Long id() {
    return this.id;
  }

  public String name() {
    return name;
  }

  public ProductCategory setName(String name) {
    this.name = name;
    return this;
  }

  public String parentId() {
    return parentId;
  }

  public ProductCategory setParentId(String parentId) {
    this.parentId = parentId;
    return this;
  }

  public Long offerId() {
    return this.offerId;
  }

  public ProductCategory setOfferId(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  public String originalId() {
    return this.originalId;
  }

  public ProductCategory setOriginalId(String originalId) {
    this.originalId = originalId;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(ProductCategory.class)
        .add("id", id)
        .add("originalId", originalId)
        .add("offerId", offerId)
        .add("name", name)
        .add("parentId", parentId).toString();
  }
}
