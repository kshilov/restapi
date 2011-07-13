package com.heymoose.rest.domain.poll;

import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "choice")
public class Choice extends IdEntity {

  @ManyToOne
  private Poll poll;

  public Choice(Poll poll) {
    this.poll = poll;
  }

  public Poll poll() {
    return poll;
  }
}
