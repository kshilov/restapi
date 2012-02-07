package com.heymoose.domain;

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
import javax.persistence.OneToOne;
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


  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "timestamp", nullable = true)
  private DateTime timestamp;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "last_tx", nullable = false)
  private AccountTx lastTx;

  @Override
  public Long id() {
    return id;
  }

  protected Withdraw() { }

  public Withdraw(Account account, AccountTx lastTx) {
    this.account = account;
    this.timestamp = DateTime.now();
    this.lastTx = lastTx;
  }

  public Account account() {
    return account;
  }

  public DateTime timestamp() {
    return timestamp;
  }

  public AccountTx lastTx() {
    return lastTx;
  }
}
