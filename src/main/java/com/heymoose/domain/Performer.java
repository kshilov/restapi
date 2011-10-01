package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(
    name = "performer",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ext_id", "app_id"})
)
public class Performer extends IdEntity {

  @Column(name = "ext_id")
  private String extId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_id", nullable = false)
  private App app;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inviter", nullable = true)
  private Performer inviter;

  protected Performer() {}

  public Performer(String extId, App app, Performer inviter) {
    checkNotNull(extId, app);
    this.extId = extId;
    this.app = app;
    if (inviter.equals(this))
      throw new IllegalArgumentException();
    this.inviter = inviter;
    this.creationTime = DateTime.now();
  }

  public App app() {
    return app;
  }

  public String extId() {
    return extId;
  }
}
