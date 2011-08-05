package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "question")
public class Question extends SingleQuestion<Answer> {
  protected Question() {}
  public Question(String text) {
    super(text);
  }
}
