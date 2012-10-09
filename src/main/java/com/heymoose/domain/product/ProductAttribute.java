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
@Table(name = "product_attribute")
public class ProductAttribute extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute-seq")
  @SequenceGenerator(name = "product_attribute-seq", sequenceName = "product_attribute_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "product_id", nullable = false)
  protected Long productId;

  @Column(nullable = false)
  protected String key;

  @Column(nullable = false)
  protected String value;

  @Override
  public Long id() {
    return this.id;
  }

  public Long productId() {
    return productId;
  }

  public ProductAttribute setProductId(Long productId) {
    this.productId = productId;
    return this;
  }

  public String key() {
    return key;
  }

  public ProductAttribute setKey(String key) {
    this.key = key;
    return this;
  }

  public String value() {
    return value;
  }

  public ProductAttribute setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(ProductAttribute.class)
        .add("productId", productId)
        .add("key", key)
        .add("value", value)
        .toString();
  }
}
