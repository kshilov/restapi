package com.heymoose.rest.domain.app;


import com.heymoose.rest.domain.account.Account;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "application")
public class App {

  @Id
  private Integer id;

  @Basic(optional = false)
  private String secret;

  @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  public App(int id, String secret) {
    this.id = id;
    this.secret = secret;
    this.account = new Account();
  }

  private App() {}

  public Account account() {
    return account;
  }

  public String secret() {
    return secret;
  }

  public Integer id() {
    return id;
  }

  public void refreshSecret(String secret) {
    this.secret = secret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof App)) return false;

    App app = (App) o;

    if (id != null ? !id.equals(app.id) : app.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
