package com.heymoose.domain;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.base.IdEntity;
import java.math.BigDecimal;
import javax.persistence.Basic;
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
@Table(name = "withdraw")
public class Withdraw extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "withdraw-seq")
  @SequenceGenerator(name = "withdraw-seq", sequenceName = "withdraw_seq", allocationSize = 1)
  protected Long id;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Basic
  private BigDecimal amount;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "timestamp", nullable = true)
  private DateTime timestamp;

  @Basic
  private Boolean done = false;

  @Override
  public Long id() {
    return id;
  }

  protected Withdraw() { }

  public Withdraw(Account account, BigDecimal amount) {
    this.account = account;
    this.amount = amount;
    this.timestamp = DateTime.now();
  }

  public Account account() {
    return account;
  }

  public DateTime timestamp() {
    return timestamp;
  }

  public void approve() {
    done = true;
  }

  public BigDecimal amount() {
    return amount;
  }

  public boolean done() {
    return done;
  }
}
