package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "city")
public class City extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city-seq")
  @SequenceGenerator(name = "city-seq", sequenceName = "city_seq", allocationSize = 1)
  protected Long id;

  @Basic(optional = false)
  private String name;

  @Override
  public Long id() {
    return id;
  }

  protected City() {}


  public City(String name) {
    checkNotNull(name);
    this.name = name;
  }

  public String name() {
    return name;
  }

  public void changeName(String name) {
    checkNotNull(name);
    this.name = name;
  }
}
