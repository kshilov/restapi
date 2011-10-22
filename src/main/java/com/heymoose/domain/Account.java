package com.heymoose.domain;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.SortedSet;

@Entity
@Table(name = "account")
public class Account extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account-seq")
  @SequenceGenerator(name = "account-seq", sequenceName = "account_seq", allocationSize = 1)
  protected Long id;

  public Long id() {
    return id;
  }

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", fetch = FetchType.LAZY)
  @Sort(type = SortType.NATURAL)
  private SortedSet<AccountTx> transactions;

  public Account() {}

  public Account(BigDecimal balance) {
    assertTransactions();
    transactions.add(new AccountTx(this, balance));
  }

  private void assertTransactions() {
    if (transactions == null)
      transactions = Sets.newTreeSet();
  }

  public AccountTx currentState() {
    assertTransactions();
    if (transactions.isEmpty())
      return null;
    return transactions.last();
  }

  public AccountTx addToBalance(BigDecimal amount, String description) {
    assertTransactions();
    AccountTx tx = currentState() == null ?
                    new AccountTx(this, amount) : currentState().add(amount, description);
    transactions.add(tx);
    return tx;
  }

  public AccountTx subtractFromBalance(BigDecimal amount, String description) {
    if (currentState() == null)
      throw new IllegalStateException("No enough money");
    AccountTx tx = currentState().subtract(amount, description);
    assertTransactions();
    transactions.add(tx);
    return  tx;
  }
}
