package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "single_question")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SingleQuestion<T extends AnswerBase<? extends SingleQuestion>> extends QuestionBase<T, Order> {

  @Basic
  private String text;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "order_id")
  protected Order order;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "form_id")
  private Form form;

  public String text() {
    return text;
  }

  protected SingleQuestion() {}

  public SingleQuestion(String text) {
    this.text = text;
  }

  @Override
  public Order order() {
    return order;
  }

  @Override
  public void setOrder(Order order) {
    this.order = order;
  }

  public boolean hashOrder() {
    return order != null;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  public Form form() {
    return form;
  }

  public boolean hasForm() {
    return form != null;
  }
}
