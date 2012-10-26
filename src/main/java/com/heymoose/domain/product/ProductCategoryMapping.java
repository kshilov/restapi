package com.heymoose.domain.product;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "product_category")
public final class ProductCategoryMapping extends IdEntity {


  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_category-seq")
  @SequenceGenerator(name = "product_category-seq", sequenceName = "product_category_seq", allocationSize = 1)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "shop_category_id")
  private ShopCategory category;

  @Column(name = "is_direct")
  private boolean isDirect = true;

  public ProductCategoryMapping setCategory(ShopCategory category) {
    this.category = category;
    return this;
  }

  public ProductCategoryMapping setProduct(Product product) {
    this.product = product;
    return this;
  }

  public ProductCategoryMapping isNotDirect() {
    this.isDirect = false;
    return this;
  }

  public Product product() {
    return product;
  }

  public ShopCategory category() {
    return category;
  }

  public boolean isDirect() {
    return isDirect;
  }

  @Override
  public Long id() {
    return id;
  }
}
