package com.heymoose.rest.domain.poll;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "vote")
public class Vote extends BaseAnswer<Poll> {

  @ManyToOne
  private Choice choice;

  public Vote(Poll question, Choice choice) {
    super(question);
    this.choice = choice;
  }

  public Choice choice() {
    return choice;
  }
}
