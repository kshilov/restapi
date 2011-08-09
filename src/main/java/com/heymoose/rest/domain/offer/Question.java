package com.heymoose.rest.domain.offer;

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
