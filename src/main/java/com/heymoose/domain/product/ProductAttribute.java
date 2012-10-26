package com.heymoose.domain.product;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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
import java.util.Map;

@Entity
@Table(name = "product_attribute")
public class ProductAttribute extends IdEntity {

  private static final char FIELD_SEPARATOR = '&';
  private static final char KEY_VAL_SEPARATOR = '=';

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute-seq")
  @SequenceGenerator(name = "product_attribute-seq", sequenceName = "product_attribute_seq", allocationSize = 1)
  protected Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  protected Product product;

  @Column(nullable = false)
  protected String key;

  @Column(nullable = false)
  protected String value;

  @Column(name = "extra_info", nullable = true)
  protected String extraInfo;

  @Override
  public Long id() {
    return this.id;
  }


  public ProductAttribute setProduct(Product product) {
    this.product = product;
    return this;
  }


  public Product product() {
    return product;
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
        .add("productId", product == null ? null : product.id())
        .add("key", key)
        .add("value", value)
        .add("extra", extraInfo)
        .toString();
  }

  public ProductAttribute addExtraInfo(String name, String value) {
    this.extraInfo = Joiner.on(FIELD_SEPARATOR).skipNulls()
        .join(this.extraInfo, Joiner.on(KEY_VAL_SEPARATOR).join(name, value));
    return this;
  }

  public Map<String, String> extraInfo() {
    return Splitter.on(FIELD_SEPARATOR)
        .omitEmptyStrings()
        .withKeyValueSeparator(Splitter.on(KEY_VAL_SEPARATOR))
        .split(Strings.nullToEmpty(this.extraInfo));
  }

  public String extraInfoString() {
    return this.extraInfo;
  }

}
