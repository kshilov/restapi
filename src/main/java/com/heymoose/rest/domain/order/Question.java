package com.heymoose.rest.domain.order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Question {
  @Id
  @GeneratedValue
  private Integer id;

  @Basic
  private String text;

  @ManyToOne
  private Order order;

  private Question() {}

  public Question(String text, Order order) {
    this.text = text;
    this.order = order;
  }

  public Integer id() {
    return id;
  }

  public String text() {
    return text;
  }

  public Order order() {
    return order;
  }
}
