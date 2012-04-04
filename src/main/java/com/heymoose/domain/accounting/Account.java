package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.affiliate.base.ModifiableEntity;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "account")
public class Account extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account-seq")
  @SequenceGenerator(name = "account-seq", sequenceName = "account_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private BigDecimal balance = new BigDecimal(0);

  @Version
  private Integer version;

  @Column(name = "allow_negative_balance")
  private boolean allowNegativeBalance;

  @Override
  public Long id() {
    return id;
  }

  public Account() {
    this(false);
  }

  public Account(boolean allowNegativeBalance) {
    this.allowNegativeBalance = allowNegativeBalance;
  }

  public BigDecimal balance() {
    return balance;
  }

  public boolean allowNegativeBalance() {
    return allowNegativeBalance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
    touch();
  }

  public AccountingEntry add(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    return new AccountingEntry(this, amount);
  }

  public AccountingEntry subtract(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    return new AccountingEntry(this, amount.negate());
  }
}
