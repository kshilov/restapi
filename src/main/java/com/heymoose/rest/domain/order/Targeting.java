package com.heymoose.rest.domain.order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Targeting {
  @Id
  @GeneratedValue
  private Integer id;

  @Basic
  private Integer age;

  @Basic
  private Boolean male;

  @Basic
  private String city;

  @Basic
  private String country;

  private Targeting() {}

  public Targeting(Integer age, Boolean male, String city, String country) {
    this.age = age;
    this.male = male;
    this.city = city;
    this.country = country;
  }

  public Integer id() {
    return id;
  }

  public Boolean male() {
    return male;
  }

  public String city() {
    return city;
  }

  public String county() {
    return country;
  }
}
