package com.heymoose.domain.statistics;

import com.heymoose.domain.base.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "lead_stat")
public final class LeadStat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_stat-seq")
  @SequenceGenerator(name = "lead_stat-seq", sequenceName = "lead_stat_seq", allocationSize = 1)
  private Long id;

  @Basic
  private String ip;

  @ManyToOne
  @JoinColumn(name = "token_id")
  private Token token;

  @Basic
  private String referrer;

  @Column(name = "lead_key", nullable = false)
  private String leadKey;

  @Basic
  private String method;

  @Override
  public Long id() {
    return id;
  }

  public Token token() {
    return token;
  }

  public String ip() {
    return ip;
  }

  public String referrer() {
    return referrer;
  }

  public String key() {
    return leadKey;
  }

  public LeadStat setKey(String leadKey) {
    this.leadKey = leadKey;
    return this;
  }

  public LeadStat setToken(Token token) {
    this.token = token;
    return this;
  }

  public LeadStat setIp(String ip) {
    this.ip = ip;
    return this;
  }

  public LeadStat setReferrer(String referrer) {
    this.referrer = referrer;
    return this;
  }

  public String method() {
    return this.method;
  }

  public LeadStat setMethod(String method) {
    this.method = method;
    return this;
  }
}
