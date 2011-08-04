package com.heymoose.rest.domain.order;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.question.QuestionBase;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order extends OrderBase {
  
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  protected Set<QuestionBase> questions;

  protected Order() {}

  public Order(BigDecimal balance, String name, Targeting targeting, BigDecimal answerCost, Iterable<QuestionBase> questions) {
    super(balance, name, targeting, answerCost);
    this.name = name;
    this.account = new Account(balance);
    this.targeting = targeting;
    this.answerCost = answerCost;
    this.questions = Sets.newHashSet(questions);
    for (QuestionBase q  : this.questions)
      q.setOrder(this);
  }

  public Set<QuestionBase> questions() {
    if (questions == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(questions);
  }
}
