package com.heymoose.domain.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@MappedSuperclass
public abstract class ModifiableEntity extends BaseEntity {

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "last_change_time")
  private DateTime lastChangeTime;

  protected void touch() {
    lastChangeTime = DateTime.now();
  }
}
