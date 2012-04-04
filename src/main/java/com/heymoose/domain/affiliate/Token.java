package com.heymoose.domain.affiliate;

import com.heymoose.domain.affiliate.base.BaseEntity;
import java.math.BigInteger;
import java.util.Random;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "token")
public class Token extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token-seq")
  @SequenceGenerator(name = "token-seq", sequenceName = "token_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String value;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "stat_id")
  private OfferStat stat;

  @Basic(optional = false)
  private boolean used;

  @Override
  public Long id() {
    return id;
  }

  protected Token() {}

  public Token(OfferStat stat) {
    this.stat = stat;
    Random random = new Random();
    this.value = new BigInteger(160, random).toString(32);
  }

  public OfferStat stat() {
    return stat;
  }

  public String value() {
    return value;
  }

  public boolean used() {
    return used;
  }

  public void markAsUsed() {
    used = true;
  }
}
