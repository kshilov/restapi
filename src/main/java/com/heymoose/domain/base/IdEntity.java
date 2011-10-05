package com.heymoose.domain.base;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class IdEntity {

  public abstract Long id();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IdEntity)) return false;

    IdEntity idEntity = (IdEntity) o;

    if (id() != null ? !id().equals(idEntity.id()) : idEntity.id() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id() != null ? id().hashCode() : 0;
  }
}
