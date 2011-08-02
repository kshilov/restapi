package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.BaseOrder;
import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_question")
public abstract class BaseQuestion<T extends BaseAnswer> extends Reservable<Order> {

  @Basic
  private String text;

  @ManyToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, targetEntity = Order.class)
  @JoinColumn(name = "order_id")
  protected Order order;

  @ManyToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "form_id")
  private Form form;

  @OneToMany(targetEntity = BaseAnswer.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "question")
  private Set<T> answers;

  protected BaseQuestion() {}

  public BaseQuestion(String text) {
    this.text = text;
  }

  public String text() {
    return text;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  public Form form() {
    return form;
  }

  public boolean hashOrder() {
    return order != null;
  }

  public boolean hasForm() {
    return form != null;
  }

  @Override
  public Order order() {
    return order;
  }

  @Override
  public void setOrder(Order order) {
    this.order = order;
  }

  public Set<T> answers() {
    if (answers == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(answers);
  }

  public void addAnswer(T answer) {
    if (answers == null)
      answers = Sets.newHashSet();
    answers.add(answer);
  }
}
