package com.heymoose.domain.base;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.builder.CompareToBuilder;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Map;

@MappedSuperclass
public abstract class IdEntity implements Comparable<IdEntity>, Serializable {


  public static <T extends IdEntity> Map<Long, T> toMap(Iterable<T> list) {
    ImmutableMap.Builder<Long, T> builder = ImmutableMap.builder();
    for (T entity : list) {
      builder.put(entity.id(), entity);
    }
    return builder.build();
  }


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
