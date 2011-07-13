package com.heymoose.rest.domain.order;

import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "targeting")
public class Targeting extends IdEntity {

  @Basic
  private Integer age;

  @Basic
  private Boolean male;

  @Basic
  private String city;

  @Basic
  private String country;

  private Targeting() {}

  public Targeting(int age, boolean male, String city, String country) {
    this.age = age;
    this.male = male;
    this.city = city;
    this.country = country;
  }

  public int age() {
    return age;
  }

  public boolean male() {
    return male;
  }

  public String city() {
    return city;
  }

  public String county() {
    return country;
  }
}
