package com.heymoose.rest.domain.poll;

import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_question")
public abstract class BaseQuestion extends IdEntity {

  private final static int DEFAULT_MAX_ASKED = 1000;

  @Basic
  private String text;

  @ManyToOne
  private Order order;

  @Basic
  private int asked;

  private BaseQuestion() {}

  public BaseQuestion(String text) {
    this.text = text;
  }

  public String text() {
    return text;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Order order() {
    return order;
  }

  public boolean active() {
    return asked < DEFAULT_MAX_ASKED;
  }

  public void ask() {
    asked++;
  }
}
