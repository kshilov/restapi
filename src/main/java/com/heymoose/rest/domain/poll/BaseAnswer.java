package com.heymoose.rest.domain.poll;

import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_answer")
public class BaseAnswer<T extends BaseQuestion> extends IdEntity {

  @ManyToOne(targetEntity = BaseQuestion.class)
  @JoinColumn(name = "question_id")
  private T question;

  @Basic
  private boolean accepted;

  private BaseAnswer(){ }
  
  public BaseAnswer(T question) {
    this.question = question;
  }

  public T question() {
    return question;
  }

  public void markAsAccepted() {
    accepted = true;
  }
}
