package com.heymoose.rest.domain.order;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.question.BaseQuestion;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "poll_order")
public class Order extends IdEntity {

  @Basic
  private String name;

  @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<BaseQuestion> questions;

  @OneToOne(cascade = CascadeType.ALL)
  private Targeting targeting;

  @Column(name = "answer_cost")
  private BigDecimal answerCost;

  @Basic
  private boolean questionary;

  protected Order() {}

  public Order(BigDecimal balance, String name, Targeting targeting, BigDecimal answerCost, Iterable<BaseQuestion> questions, boolean questionary) {
    this.name = name;
    this.account = new Account(balance);
    this.targeting = targeting;
    this.answerCost = answerCost;
    this.questions = Sets.newHashSet(questions);
    for (BaseQuestion q  : this.questions)
      q.setOrder(this);
    this.questionary = questionary;
  }

  public String name() {
    return name;
  }

  public Account account() {
    return account;
  }

  public Set<BaseQuestion> questions() {
    if (questions == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(questions);
  }

  public boolean questionary() {
    return questionary;
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
