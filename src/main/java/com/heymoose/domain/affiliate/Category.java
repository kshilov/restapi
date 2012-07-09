package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "category")
public class Category extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category-seq")
  @SequenceGenerator(name = "category-seq", sequenceName = "category_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @OneToMany(targetEntity = SubCategory.class, fetch = FetchType.LAZY,
      cascade = CascadeType.ALL, mappedBy = "categoryId")
  private Set<SubCategory> subCategoryList;

  @Override
  public Long id() {
    return id;
  }
  
  protected Category() {}
  
  public Category(Long id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public String name() {
    return name;
  }
}
