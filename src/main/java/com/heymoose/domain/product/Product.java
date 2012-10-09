package com.heymoose.domain.product;

import com.google.common.base.Objects;
import com.heymoose.domain.base.ModifiableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product-seq")
  @SequenceGenerator(name = "product-seq", sequenceName = "product_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "category_original_id")
  protected String categoryOriginalId;

  @Column(name = "offer_id", nullable = false)
  protected Long offerId;

  @Column(nullable = false)
  protected String name;

  @Column(nullable = false)
  protected String url;

  @Column(name = "original_id", nullable = false)
  private String originalId;

  @Column(name = "price")
  private BigDecimal price;

  public Product() {
    super();
  }

  @Override
  public Long id() {
    return this.id;
  }


  public String originalId() {
    return this.originalId;
  }

  public Product setOriginalId(String originalId) {
    this.originalId = originalId;
    return this;
  }

  public String name() {
    return this.name;
  }

  public Product setName(String name) {
    this.name = name;
    return this;
  }

  public String url() {
    return this.url;
  }

  public Product setUrl(String url) {
    this.url = url;
    return this;
  }

  public String categoryOriginalId() {
    return this.categoryOriginalId;
  }

  public Product setCategoryOriginalId(String categoryId) {
    this.categoryOriginalId = categoryId;
    return this;
  }

  public Long offerId() {
    return offerId;
  }

  public Product setOfferId(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  public BigDecimal price() {
    return price;
  }

  public Product setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(Product.class)
        .add("id", id)
        .add("originalId", originalId)
        .add("offerId", offerId)
        .add("categoryOriginalId", categoryOriginalId)
        .add("name", name).toString();
  }
}
