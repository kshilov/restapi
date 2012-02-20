package com.heymoose.domain;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "app_stat")
public class AppStat {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app-stat-seq")
  @SequenceGenerator(name = "app-stat-seq", sequenceName = "app_stat_seq", allocationSize = 1)
  private Long id;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "app_id")
  private App app;
  
  @Column(name = "shows_overall")
  private Long showsOverall;
  
  @Column(name = "actions_overall")
  private Long actionsOverall;
  
  @Column(name = "dau_average")
  private BigDecimal dauAverage;
  
  @Column(name = "dau_day0")
  private Long dauDay0;
  
  @Column(name = "dau_day1")
  private Long dauDay1;
  
  @Column(name = "dau_day2")
  private Long dauDay2;
  
  @Column(name = "dau_day3")
  private Long dauDay3;
  
  @Column(name = "dau_day4")
  private Long dauDay4;
  
  @Column(name = "dau_day5")
  private Long dauDay5;
  
  @Column(name = "dau_day6")
  private Long dauDay6;
  
  public AppStat() {}
  
  public AppStat(App app) {
    this.app = app;
  }
  
  public Long id() {
    return id;
  }
  
  public App app() {
    return app;
  }
  
  public Long showsOverall() {
    return showsOverall;
  }
  
  public Long actionsOverall() {
    return actionsOverall;
  }
  
  public BigDecimal dauAverage() {
    return dauAverage;
  }
  
  public Long dauDay0() {
    return dauDay0;
  }
  
  public Long dauDay1() {
    return dauDay1;
  }
  
  public Long dauDay2() {
    return dauDay2;
  }
  
  public Long dauDay3() {
    return dauDay3;
  }
  
  public Long dauDay4() {
    return dauDay4;
  }
  
  public Long dauDay5() {
    return dauDay5;
  }
  
  public Long dauDay6() {
    return dauDay6;
  }
}
