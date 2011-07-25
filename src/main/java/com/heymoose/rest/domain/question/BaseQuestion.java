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
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_question")
public abstract class BaseQuestion extends Reservable {

  @Basic
  private String text;

  @ManyToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "form_id")
  private Form form;

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
}
