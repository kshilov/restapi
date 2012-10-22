package com.heymoose.domain.product;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.tariff.Tariff;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
@NotThreadSafe
public class Product extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product-seq")
  @SequenceGenerator(name = "product-seq", sequenceName = "product_seq", allocationSize = 1)
  protected Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", nullable = false)
  protected Offer offer;

  @Column(nullable = false)
  protected String name;

  @Column(nullable = false)
  protected String url;

  @Column(name = "original_id", nullable = false)
  private String originalId;

  @Column(name = "price")
  private BigDecimal price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shop_category_id")
  private ShopCategory category;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "product",
      cascade = CascadeType.ALL)
  private List<ProductAttribute> attributeList = Lists.newArrayList();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tariff_id")
  private Tariff tariff;

  private transient Multimap<String, ProductAttribute> attributeMap;


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

  public Offer offer() {
    return offer;
  }

  public Product setOffer(Offer offer) {
    this.offer = offer;
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
        .add("offerId", offer.id())
        .add("categoryId", category == null ? null : category.id())
        .add("name", name).toString();
  }

  public ShopCategory category() {
    return this.category;
  }

  public Product setCategory(ShopCategory category) {
    this.category = category;
    return this;
  }

  public Iterable<ProductAttribute> attributes() {
    return ImmutableList.copyOf(this.attributeList);
  }

  public Product addAttribute(ProductAttribute attribute) {
    attributeMap = null;
    this.attributeList.add(attribute);
    return this;
  }

  public Product addAttribute(String key, String value) {
    return addAttribute(new ProductAttribute()
        .setProduct(this)
        .setKey(key)
        .setValue(value));
  }

  public Tariff tariff() {
    return this.tariff;
  }

  public Product setTariff(Tariff tariff) {
    this.tariff = tariff;
    return this;
  }

  public String attributeValue(String key) {
    return attributeList(key).iterator().next().value();
  }

  public Iterable<ProductAttribute> attributeList(String key) {
    if (attributeMap == null) {
      ImmutableMultimap.Builder<String, ProductAttribute> builder =
          ImmutableMultimap.builder();
      for (ProductAttribute att: attributeList) {
        builder.put(att.key(), att);
      }
      this.attributeMap = builder.build();
    }
    return attributeMap.get(key);
  }

}
