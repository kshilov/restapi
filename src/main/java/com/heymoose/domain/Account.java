package com.heymoose.domain;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;
import java.math.BigDecimal;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "account")
public class Account extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account-seq")
  @SequenceGenerator(name = "account-seq", sequenceName = "account_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "allow_negative_balance")
  private boolean allowNegativeBalance;

  public Long id() {
    return id;
  }

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", fetch = FetchType.LAZY)
  private Set<AccountTx> transactions;

  protected Account() {}

  public Account(boolean allowNegativeBalance) {
    this.allowNegativeBalance = allowNegativeBalance;
  }

  public Account(BigDecimal balance, boolean allowNegativeBalance, TxType type) {
    this(allowNegativeBalance);
    if (transactions == null)
      transactions = Sets.newTreeSet();
    transactions.add(new AccountTx(this, balance, type));
  }

  public boolean allowNegativeBalance() {
    return allowNegativeBalance;
  }
  
  public void setAllowNegativeBalance(boolean allow) {
    this.allowNegativeBalance = allow;
  }

  public Set<AccountTx> transactions() {
    return transactions;
  }
}
