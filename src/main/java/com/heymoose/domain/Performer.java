package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

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
  private Integer year;

  @Basic
  private String city;

  public static class Info {
    
    public final Boolean male;
    public final Integer year;
    public final String city;

    public Info(Boolean male, Integer year, String city) {
      if (year != null)
        checkArgument(year > 0);
      this.male = male;
      this.year = year;
      this.city = city;
    }
  }

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
  
  public DateTime creationTime() {
    return creationTime;
  }
  
  public Performer inviter() {
    return inviter;
  }

  public Boolean male() {
    return male;
  }

  public Integer year() {
    return year;
  }

  public String city() {
    return city;
  }

  public void setInfo(Info info) {
    this.male = info.male;
    this.year = info.year;
    this.city = info.city;
  }
}
