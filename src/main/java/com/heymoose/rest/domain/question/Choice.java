package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "choice")
public class Choice extends IdEntity {

  @ManyToOne
  private Poll poll;

  @Basic
  private String text;

  private Choice() {}

  public Choice(String text) {
    this.text = text;
  }

  public void setPoll(Poll poll) {
    this.poll = poll;
  }

  public Poll poll() {
    return poll;
  }

  public String text() {
    return text;
  }
}
