package com.heymoose.rest.domain.order;

import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_base")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class OrderBase extends IdEntity {

  @Basic
  protected String name;

  @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  protected Account account;

  @OneToOne(cascade = CascadeType.ALL)
  protected Targeting targeting;

  @Column(name = "answer_cost")
  protected BigDecimal answerCost;

  protected OrderBase() {}

  public OrderBase(BigDecimal balance, String name, Targeting targeting, BigDecimal answerCost) {
    this.name = name;
    this.account = new Account(balance);
    this.targeting = targeting;
    this.answerCost = answerCost;
  }
  
  public String name() {
    return name;
  }

  public Account account() {
    return account;
  }

  public Targeting targeting() {
    return targeting;
  }

  public void setTargeting(Targeting targeting) {
    this.targeting = targeting;
  }

  public BigDecimal costPerAnswer() {
    return answerCost;
  }

  public void setAnswerCost(BigDecimal newCost) {
    answerCost = newCost;
  }
}
