package com.heymoose.rest.domain.poll;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "answer")
public class Answer extends BaseAnswer<Question> {

  @Basic
  private String answer;

  public Answer(Question question, String answer) {
    super(question);
    this.answer = answer;
  }

  public String answer() {
    return answer;
  }
}
