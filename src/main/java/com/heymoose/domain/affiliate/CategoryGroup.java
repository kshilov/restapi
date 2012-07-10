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
@Table(name = "category_group")
public class CategoryGroup extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_group-seq")
  @SequenceGenerator(name = "category_group-seq", sequenceName = "category_group_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @OneToMany(targetEntity = Category.class, fetch = FetchType.LAZY,
      cascade = CascadeType.ALL, mappedBy = "categoryGroupId")
  private Set<Category> categoryList;

  @Override
  public Long id() {
    return id;
  }
  
  protected CategoryGroup() {}
  
  public CategoryGroup(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public CategoryGroup(String name) {
    this.name = name;
  }
  
  public String name() {
    return name;
  }
}
