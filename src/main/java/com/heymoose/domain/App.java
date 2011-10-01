package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.ws.rs.WebApplicationException;
import java.util.Date;
import java.util.UUID;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "app")
public class App extends IdEntity {

  @Enumerated
  @JoinColumn(name = "platform", nullable = true)
  private Platform platform;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @Basic(optional = false)
  private String secret;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Basic(optional = false)
  private boolean deleted;

  protected App() {}

  public App(User user) {
    this.user = user;
    this.secret =  UUID.randomUUID().toString();
    this.creationTime = DateTime.now();
  }

  public User owner() {
    return user;
  }

  public String secret() {
    return secret;
  }

  public void regenerateSecret() {
    secret = UUID.randomUUID().toString();
  }

  public boolean deleted() {
    return deleted;
  }

  public void delete() {
    this.deleted = true;
  }

  public void assignPlatform(Platform platform) {
    checkNotNull(platform);
    if (this.platform == null)
      this.platform = platform;
    else if (!platform.equals(this.platform))
      throw new WebApplicationException(400);
  }
}
