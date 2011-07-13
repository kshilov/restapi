package com.heymoose.rest.domain.app;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "application")
public class App {

  @Id
  private Integer id;

  @Basic(optional = false)
  private String secret;

  public App(int id, String secret) {
    this.id = id;
    this.secret = secret;
  }

  private App() {}

  public int id () {
    return id;
  }

  public String secret() {
    return secret;
  }

  public void refreshSecret(String secret) {
    this.secret = secret;
  }
}
