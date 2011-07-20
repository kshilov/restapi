package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.app.UserProfile;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "vote")
public class Vote extends BaseAnswer<Poll> {

  @ManyToOne
  private Choice choice;

  public Vote(Poll question, UserProfile user, Choice choice) {
    super(question, user);
    this.choice = choice;
  }

  public Choice choice() {
    return choice;
  }
}
