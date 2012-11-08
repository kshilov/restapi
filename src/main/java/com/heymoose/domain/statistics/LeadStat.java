package com.heymoose.domain.statistics;

import com.heymoose.domain.base.BaseEntity;

public final class LeadStat extends BaseEntity {

  private Long id;
  private String ip;
  private Token token;
  private String referrer;
  private String leadKey;

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
}
