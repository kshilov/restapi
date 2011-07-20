package com.heymoose.rest.domain.order;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.rest.domain.account.AccountTx;
import com.heymoose.rest.domain.question.Question;
import org.hibernate.Session;

@Singleton
public class Orders {

  private final static int MIN_ANSWERS_PER_PAYMENT = 5;
  
  private final Provider<Session> sessionProvider;

  @Inject
  public Orders(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  protected Session hiber() {
    return sessionProvider.get();
  }

  public void maybeExecute(Question question) {
    Long count = (Long) hiber().createQuery("select count(*) from BaseAnswer where question = :question and accepted = false")
      .setParameter("question", question)
      .uniqueResult();
    if (count >= MIN_ANSWERS_PER_PAYMENT) {
      int acceptedCount = 0;
      while (acceptedCount < count && question.order().account().actual().balance().compareTo(question.order().costPerAnswer()) != -1) {
         AccountTx tx = question.order().account().subtractFromBalance(question.order().costPerAnswer(), "answer was accepted");
         hiber().save(tx);
         acceptedCount++;
      }
      hiber().createQuery("update BaseAnswer set accepted = true where  question = :question and accepted = false").setParameter("question", question).executeUpdate();
    }
  }
}
