package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Basic;
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
@Table(name = "sub_category")
public final class SubCategory extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_category-seq")
  @SequenceGenerator(name = "sub_category-seq", sequenceName = "sub_category_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @ManyToOne(targetEntity = Category.class, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id", insertable = false, updatable = false)
  private Category category;

  protected SubCategory() { }
  public SubCategory(Long id, String name, Long categoryId) {
    this.id = id;
    this.name = name;
    this.categoryId = categoryId;
  }

  @Override
  public Long id() {
    return id;
  }
}
