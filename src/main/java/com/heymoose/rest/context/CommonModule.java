package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.account.AccountTx;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.order.BaseOrder;
import com.heymoose.rest.domain.order.FormOrder;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.question.Answer;
import com.heymoose.rest.domain.question.Answers;
import com.heymoose.rest.domain.question.BaseAnswer;
import com.heymoose.rest.domain.question.BaseQuestion;
import com.heymoose.rest.domain.question.Choice;
import com.heymoose.rest.domain.question.Form;
import com.heymoose.rest.domain.question.Poll;
import com.heymoose.rest.domain.question.Question;
import com.heymoose.rest.domain.question.Questions;
import com.heymoose.rest.domain.question.Reservable;
import com.heymoose.rest.domain.question.Vote;
import com.heymoose.rest.resource.ApiResource;
import com.heymoose.rest.resource.AppResource;
import com.heymoose.rest.resource.OrderResource;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());
    bind(AppResource.class);
    bind(OrderResource.class);
    bind(ApiResource.class);
    bind(Questions.class);
    bind(Answers.class);
    bindEntities(
            App.class,
            BaseOrder.class,
            FormOrder.class,
            Order.class,
            Targeting.class,
            BaseAnswer.class,
            BaseQuestion.class,
            Answer.class,
            Choice.class,
            Poll.class,
            Question.class,
            Vote.class,
            Account.class,
            AccountTx.class,
            Reservation.class,
            UserProfile.class,
            Form.class,
            Reservable.class
    );
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }
}
