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
@Table(name = "category")
public final class Category extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category-seq")
  @SequenceGenerator(name = "category-seq", sequenceName = "category_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @Column(name = "category_group_id", nullable = false)
  private Long categoryGroupId;

  @ManyToOne(targetEntity = CategoryGroup.class, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_group_id", insertable = false, updatable = false)
  private CategoryGroup categoryGroup;

  protected Category() { }
  public Category(Long id, String name, Long categoryGroupId) {
    this.id = id;
    this.name = name;
    this.categoryGroupId = categoryGroupId;
  }

  @Override
  public Long id() {
    return id;
  }

  public String name() {
    return name;
  }

  public CategoryGroup group() {
    return categoryGroup;
  }
}
