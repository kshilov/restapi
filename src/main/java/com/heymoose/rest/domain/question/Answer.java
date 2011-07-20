package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.app.UserProfile;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "answer")
public class Answer extends BaseAnswer<Question> {

  @Basic
  private String answer;

  public Answer(Question question, UserProfile user, String answer) {
    super(question, user);
    this.answer = answer;
  }

  public String answer() {
    return answer;
  }
}
