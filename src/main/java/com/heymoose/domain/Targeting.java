package com.heymoose.domain;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.base.IdEntity;

import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "cities_filter_type")
  private CityFilterType cityFilterType;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "targeting_city",
      joinColumns = {@JoinColumn(name = "targeting_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "city_id", referencedColumnName = "id")}
  )
  private Set<City> cities;

  protected Targeting() {}

  public Targeting(Boolean male, Integer minAge, Integer maxAge, CityTargeting cityTargeting) {
    checkArgument(minAge == null || minAge > 0);
    checkArgument(maxAge == null || maxAge > 0);
    this.male = male;
    this.minAge = minAge;
    this.maxAge = maxAge;
    if (cityTargeting != null) {
      this.cities = newHashSet(cityTargeting.cities);
      this.cityFilterType = cityTargeting.type;
    }
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
  
  public CityFilterType cityFilterType() {
    return cityFilterType;
  }
  
  public void setCityFilterType(CityFilterType filterType) {
    this.cityFilterType = filterType;
  }
  
  public Set<City> cities() {
    return cities;
  }
}
