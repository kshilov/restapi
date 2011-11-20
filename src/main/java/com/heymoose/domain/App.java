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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.util.UUID;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Entity
@Table(name = "app")
public class App extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app-seq")
  @SequenceGenerator(name = "app-seq", sequenceName = "app_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @Enumerated
  @JoinColumn(name = "platform", nullable = true)
  private Platform platform;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @Basic(optional = false)
  private String secret;

  @Column(name = "callback", nullable = false)
  private String callbackUrl;

  @Basic(optional = false)
  private String url;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Basic(optional = false)
  private boolean deleted;

  protected App() {}

  public App(User user, URI url, URI callback, Platform platform) {
    this.user = user;
    this.secret =  UUID.randomUUID().toString();
    this.creationTime = DateTime.now();
    this.url = url.toString();
    this.callbackUrl = callback.toString();
    this.platform = platform;
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

  public URI url() {
    return URI.create(url);
  }

  public URI callback() {
    return URI.create(callbackUrl);
  }
  
  public DateTime creationTime() {
    return creationTime;
  }

  public Platform platform() {
    return platform;
  }
}
