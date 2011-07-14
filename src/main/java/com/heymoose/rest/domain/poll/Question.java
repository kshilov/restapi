package com.heymoose.rest.domain.poll;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "question")
public class Question extends BaseQuestion {
  public Question(String text) {
    super(text);
  }
}
