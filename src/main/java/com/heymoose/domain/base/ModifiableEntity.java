package com.heymoose.domain.base;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModifiableEntity extends BaseEntity {

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "last_change_time")
  private DateTime lastChangeTime = DateTime.now();

  protected void touch() {
    lastChangeTime = DateTime.now();
  }
}
