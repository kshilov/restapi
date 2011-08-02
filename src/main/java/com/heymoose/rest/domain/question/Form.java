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

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, targetEntity = FormOrder.class)
  @JoinColumn(name = "form_order_id")
  protected FormOrder order;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "form")
  private Set<QuestionBase> questions;

  @Basic
  private int asked;

  private Form() {}

  public Form(Iterable<QuestionBase> questions) {
    this.questions = Sets.newHashSet(questions);
    for (QuestionBase q : this.questions)
      q.setForm(this);
  }

  public Set<QuestionBase> questions() {
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
