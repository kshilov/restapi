package com.heymoose.domain.model;

import com.heymoose.domain.model.accounting.Account;
import com.heymoose.domain.model.base.IdEntity;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "admin_account_not_confirmed")
public class AdminAccountNotConfirmed extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin-account-not-confirmed-seq")
  @SequenceGenerator(name = "admin-account-not-confirmed-seq", sequenceName = "admin_account_not_confirmed_seq", allocationSize = 1)
  private Long id;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  private Account account;

  @Override
  public Long id() {
    return id;
  }

  public AdminAccountNotConfirmed() {
    account = new Account(true);
  }

  public Account account() {
    return account;
  }
}
