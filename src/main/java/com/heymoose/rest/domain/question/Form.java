package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.order.FormOrder;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "form")
public class Form extends Reservable<FormOrder> {

  @ManyToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, targetEntity = FormOrder.class)
  @JoinColumn(name = "form_order_id")
  protected FormOrder order;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<BaseQuestion> questions;

  @Basic
  private int asked;

  private Form() {}

  public Form(Iterable<BaseQuestion> questions) {
    this.questions = Sets.newHashSet(questions);
    for (BaseQuestion q : this.questions)
      q.setForm(this);
  }

  public Set<BaseQuestion> questions() {
    if (questions == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(questions);
  }

  private void assertQuestions() {
    if (questions == null)
      questions = Sets.newHashSet();
  }

  public int asked() {
    return asked;
  }

  public void ask() {
    asked++;
  }

  @Override
  public FormOrder order() {
    return order;
  }

  @Override
  public void setOrder(FormOrder order) {
    this.order = order;
  }
}
