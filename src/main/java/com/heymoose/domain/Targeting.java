package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Table(name = "targeting")
public class Targeting extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "targeting-seq")
  @SequenceGenerator(name = "targeting-seq", sequenceName = "targeting_seq", allocationSize = 1)
  protected Long id;

  @Override
  public Long id() {
    return id;
  }

  @Basic
  private Boolean male;

  @Column(name = "min_age")
  private Integer minAge;

  @Column(name = "max_age")
  private Integer maxAge;

  protected Targeting() {}

  public Targeting(Boolean male, Integer minAge, Integer maxAge) {
    checkArgument(minAge == null || minAge > 0);
    checkArgument(maxAge == null || maxAge > 0);
    this.male = male;
    this.minAge = minAge;
    this.maxAge = maxAge;
  }

  public Boolean male() {
    return male;
  }
  
  public void setMale(Boolean male) {
    this.male = male;
  }

  public Integer minAge() {
    return minAge;
  }
  
  public void setMinAge(Integer minAge) {
    this.minAge = minAge;
  }

  public Integer maxAge() {
    return maxAge;
  }
  
  public void setMaxAge(Integer maxAge) {
    this.maxAge = maxAge;
  }
}
