package com.heymoose.domain;

import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
@Table(name = "user_stat")
public class UserStat {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-stat-seq")
  @SequenceGenerator(name = "user-stat-seq", sequenceName = "user_stat_seq", allocationSize = 1)
  private Long id;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id")
  private User user;
  
  @Basic
  private BigDecimal payments;
  
  public UserStat() {}
  
  public UserStat(User user, BigDecimal payments) {
    this.user = user;
    this.payments = payments;
  }
  
  public Long id() {
    return id;
  }
  
  public User user() {
    return user;
  }
  
  public BigDecimal payments() {
    return payments;
  }
}
