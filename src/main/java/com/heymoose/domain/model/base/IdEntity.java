package com.heymoose.domain.model.base;

import java.io.Serializable;
import javax.persistence.MappedSuperclass;
import org.apache.commons.lang.builder.CompareToBuilder;

@MappedSuperclass
public abstract class IdEntity implements Comparable<IdEntity>, Serializable {

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

  @Override
  public int compareTo(IdEntity o) {
    return new CompareToBuilder()
        .append(this.id(), o.id())
        .toComparison();
  }
}
