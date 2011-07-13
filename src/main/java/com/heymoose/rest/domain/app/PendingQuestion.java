package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.poll.BaseQuestion;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "pending_question")
public class PendingQuestion {
  @Id
  @GeneratedValue
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private BaseQuestion question;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserProfile user;

  @Temporal(TemporalType.TIMESTAMP)
  private Date creationTime;

  private PendingQuestion() {}

  public PendingQuestion(BaseQuestion question, UserProfile profile) {
    
  }
}
