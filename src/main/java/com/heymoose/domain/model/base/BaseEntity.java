package com.heymoose.domain.model.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@MappedSuperclass
public abstract class BaseEntity extends IdEntity {

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  public BaseEntity() {
    creationTime = DateTime.now();
  }

  public DateTime creationTime() {
    return creationTime;
  }
}
