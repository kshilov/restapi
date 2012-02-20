package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.base.IdEntity;
import javax.persistence.CascadeType;
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
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "app_visit")
public class AppVisit extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app-visit-seq")
  @SequenceGenerator(name = "app-visit-seq", sequenceName = "app_visit_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "performer_id", nullable = false)
  private Performer visitor;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "app_id", nullable = false)
  private App app;

  @Column(name = "first_visit_time", nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  private DateTime firstVisitTime;

  @Column(name = "last_visit_time", nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  private DateTime lastVisitTime;
  
  protected AppVisit() {}
  
  public AppVisit(Performer visitor, App app) {
    checkArgument(visitor.platform() == app.platform());
    this.visitor = visitor;
    this.app = app;
    DateTime now = DateTime.now();
    this.firstVisitTime = now;
    this.lastVisitTime = now;
  }

  @Override
  public Long id() {
    return id;
  }

  public void update() {
    lastVisitTime = DateTime.now();
  }
  
  public App app() {
    return app;
  }

  public Performer visitor() {
    return visitor;
  }

  public DateTime lastVisitTime() {
    return lastVisitTime;
  }

  public DateTime firstVisitTime() {
    return firstVisitTime;
  }
}
