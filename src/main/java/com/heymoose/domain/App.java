package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

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

  @Basic(optional = false)
  private String title;

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

  @Basic
  private BigDecimal d;

  @Basic
  private BigDecimal t;
  
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "app")
  private AppStat stat;

  protected App() {}

  public App(String title, User user, URI url, URI callback, Platform platform) {
    checkNotNull(title, user, url, callback, platform);
    this.title = title;
    this.user = user;
    this.secret =  UUID.randomUUID().toString();
    this.creationTime = DateTime.now();
    this.url = url.toString();
    this.callbackUrl = callback.toString();
    this.platform = platform;
    this.d = new BigDecimal(0.8);
    this.t = new BigDecimal(0.1);
  }

  public String title() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
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
  
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public URI url() {
    return URI.create(url);
  }
  
  public void setUrl(URI url) {
    this.url = url.toString();
  }

  public URI callback() {
    return URI.create(callbackUrl);
  }
  
  public void setCallback(URI callback) {
    this.callbackUrl = callback.toString();
  }
  
  public DateTime creationTime() {
    return creationTime;
  }

  public Platform platform() {
    return platform;
  }
  
  public void setPlatform(Platform platform) {
    this.platform = platform;
  }

  public BigDecimal D() {
    if (d == null)
      return new BigDecimal(0.8);
    return d;
  }

  public void setD(BigDecimal d) {
    this.d = d;
  }

  public BigDecimal T() {
    if (t == null)
      return new BigDecimal(0.1);
    return t;
  }

  public void setT(BigDecimal t) {
    this.t = t;
  }

  public BigDecimal calcRevenue(BigDecimal cost) {
    return cost.subtract(D()).multiply(T()).add(D());
  }
  
  public AppStat stat() {
    return stat;
  }
}
