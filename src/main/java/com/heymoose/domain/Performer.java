package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(
    name = "performer",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ext_id", "app_id"})
)
public class Performer extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "performer-seq")
  @SequenceGenerator(name = "performer-seq", sequenceName = "performer_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

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

  @Basic
  private Boolean male;

  @Basic
  private Integer age;

  protected Performer() {}

  public Performer(String extId, App app, Performer inviter) {
    checkNotNull(extId, app);
    this.extId = extId;
    this.app = app;
    if (inviter != null && inviter.equals(this))
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

  public Boolean male() {
    return male;
  }

  public Integer age() {
    return age;
  }

  public void setInfo(boolean male, int age) {
    checkArgument(age > 0);
    this.male = male;
    this.age = age;
  }
}
