package com.heymoose.rest.domain.order;

import com.heymoose.rest.domain.question.Form;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "form_order")
public class FormOrder extends BaseOrder {

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
  private Form form;

  protected FormOrder() {}

  public FormOrder(BigDecimal balance, String name, Targeting targeting, BigDecimal answerCost, Form form){
    super(balance, name, targeting, answerCost);
    this.form = form;
    this.form.setOrder(this);
  }

  public Form form() {
    return form;
  }
}
