package com.heymoose.rest.domain.account;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.heymoose.rest.domain.base.IdEntity;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.SortedSet;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "account_owner")
public abstract class AccountOwner<T extends AccountOwner> extends IdEntity {

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.LAZY)
  @Sort(type = SortType.NATURAL)
  private SortedSet<AccountTx<T>> transactions;

  protected AccountOwner(){}

  public AccountOwner(BigDecimal balance) {
    transactions = Sets.newTreeSet();
    transactions.add(new AccountTx<T>((T)this, balance));
  }

  public AccountTx<T> account() {
    return transactions.last();
  }

  protected void changeAccount(AccountTx<T> account) {
    if (transactions.last().version() >= account.version())
      throw new IllegalArgumentException("Old transaction");
    transactions.add(account);
  }

  public AccountTx<T> addToBalance(BigDecimal amount, String description) {
    AccountTx tx =  account().add(amount, description);
    changeAccount(tx);
    return tx;
  }

  public AccountTx<T> subtractFromBalance(BigDecimal amount, String description) {
    AccountTx tx = account().subtract(amount, description);
    changeAccount(tx);
    return  tx;
  }

  @Override
  public String toString() {
    return Objects
            .toStringHelper(this)
            .add("id", id())
            .add("balance", account().balance())
            .toString();
  }
}
