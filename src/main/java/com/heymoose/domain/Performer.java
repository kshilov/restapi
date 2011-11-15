package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(
    name = "performer",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ext_id", "platform"})
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

  @Enumerated
  @JoinColumn(name = "platform", nullable = true)
  private Platform platform;

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

  public Performer(String extId, Platform platform, Performer inviter) {
    checkNotNull(extId, platform);
    this.extId = extId;
    this.platform = platform;
    if (inviter != null && inviter.equals(this))
      throw new IllegalArgumentException();
    this.inviter = inviter;
    this.creationTime = DateTime.now();
  }

  public Platform platform() {
    return platform;
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
