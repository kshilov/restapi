package com.heymoose.rest.domain.account;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.base.IdEntity;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.SortedSet;

@Entity
@Table(name = "account")
public class Account extends IdEntity {

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

  public AccountTx actual() {
    assertTransactions();
    if (transactions.isEmpty())
      return null;
    return transactions.last();
  }

  public AccountTx addToBalance(BigDecimal amount, String description) {
    AccountTx tx =  actual().add(amount, description);
    assertTransactions();
    transactions.add(tx);
    return tx;
  }

  public AccountTx subtractFromBalance(BigDecimal amount, String description) {
    AccountTx tx = actual().subtract(amount, description);
    assertTransactions();
    transactions.add(tx);
    return  tx;
  }
}
