package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.account.AccountTx;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.order.FormOrder;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.OrderBase;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.offer.Answer;
import com.heymoose.rest.domain.offer.Result;
import com.heymoose.rest.domain.offer.Answers;
import com.heymoose.rest.domain.offer.Choice;
import com.heymoose.rest.domain.offer.FilledForm;
import com.heymoose.rest.domain.offer.Form;
import com.heymoose.rest.domain.offer.Poll;
import com.heymoose.rest.domain.offer.Question;
import com.heymoose.rest.domain.offer.Offer;
import com.heymoose.rest.domain.offer.Questions;
import com.heymoose.rest.domain.offer.SingleQuestion;
import com.heymoose.rest.domain.offer.Vote;
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
            OrderBase.class,
            FormOrder.class,
            Order.class,
            Targeting.class,
            Result.class,
            Offer.class,
            Answer.class,
            Choice.class,
            Poll.class,
            SingleQuestion.class,
            Question.class,
            Vote.class,
            Account.class,
            AccountTx.class,
            Reservation.class,
            UserProfile.class,
            FilledForm.class,
            Form.class//,
//            Reservable.class
    );
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }
}
