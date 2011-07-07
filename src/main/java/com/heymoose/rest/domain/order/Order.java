package com.heymoose.rest.domain.order;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "poll_order")
public class Order {
  @Id
  @GeneratedValue
  private Integer id;

  @Basic
  private BigDecimal balance;

  @Basic
  private String name;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private Set<Question> questions;

  @OneToOne(cascade = CascadeType.ALL)
  private Targeting targeting;

  private Order() {}

  public Order(BigDecimal balance, String name) {
    this.balance = balance;
    this.name = name;
  }

  public Integer id() {
    return id;
  }

  public BigDecimal balance() {
    return balance;
  }

  public String name() {
    return name;
  }

  public Set<Question> questions() {
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
}
