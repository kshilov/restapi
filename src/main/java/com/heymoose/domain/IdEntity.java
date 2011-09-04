package com.heymoose.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class IdEntity {
  
  @Id
  @GeneratedValue
  private Long id;

  public Long id() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IdEntity)) return false;

    IdEntity idEntity = (IdEntity) o;

    if (id != null ? !id.equals(idEntity.id) : idEntity.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
