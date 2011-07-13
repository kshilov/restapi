package com.heymoose.rest.domain.poll;

import com.heymoose.rest.domain.order.Order;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "question")
public class Question extends BaseQuestion {
  public Question(String text, Order order) {
    super(text, order);
  }
}
