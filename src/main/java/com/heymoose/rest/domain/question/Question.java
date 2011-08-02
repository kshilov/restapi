package com.heymoose.rest.domain.question;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "question")
public class Question extends QuestionBase<Answer> {
  private Question() {}
  public Question(String text) {
    super(text);
  }
}
