package com.heymoose.rest.domain.question;

import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_answer")
public abstract class BaseAnswer<T extends BaseQuestion> extends IdEntity {

  @ManyToOne(targetEntity = BaseQuestion.class, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id")
  private T question;

  @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private UserProfile user;

  protected BaseAnswer(){}
  
  public BaseAnswer(T question, UserProfile user) {
    this.question = question;
    this.question.addAnswer(this);
    this.user = user;
  }

  public T question() {
    return question;
  }

  public UserProfile user() {
    return user;
  }
}
