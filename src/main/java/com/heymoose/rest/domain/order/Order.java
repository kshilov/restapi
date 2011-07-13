package com.heymoose.rest.domain.order;

import com.heymoose.rest.domain.account.AccountOwner;
import com.heymoose.rest.domain.poll.BaseQuestion;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "poll_order")
public class Order extends AccountOwner<Order> {

  @Basic
  private String name;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<BaseQuestion> questions;

  @OneToOne(cascade = CascadeType.ALL)
  private Targeting targeting;

  @Column(name = "cost_per_answer")
  private BigDecimal costPerAnswer;

  private Order() {}

  public Order(BigDecimal balance, String name, Targeting targeting, BigDecimal costPerAnswer) {
    super(balance);
    this.name = name;
    this.targeting = targeting;
    this.costPerAnswer = costPerAnswer;
  }

  public String name() {
    return name;
  }

  public Set<BaseQuestion> questions() {
    if (questions == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(questions);
  }

  public Targeting targeting() {
    return targeting;
  }

  public void setTargeting(Targeting targeting) {
    this.targeting = targeting;
  }

  public BigDecimal costPerAnswer() {
    return costPerAnswer;
  }

  public void setCostPerAnswer(BigDecimal newCost) {
    costPerAnswer = newCost;     
  }
}
