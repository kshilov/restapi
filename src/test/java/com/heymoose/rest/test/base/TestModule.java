package com.heymoose.rest.test.base;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.account.AccountTx;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.poll.BaseAnswer;
import com.heymoose.rest.domain.poll.BaseQuestion;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.poll.Answer;
import com.heymoose.rest.domain.poll.Choice;
import com.heymoose.rest.domain.poll.Poll;
import com.heymoose.rest.domain.poll.Question;
import com.heymoose.rest.domain.poll.Vote;
import org.hibernate.cfg.Configuration;
import org.junit.Ignore;

import java.util.Properties;

@Ignore
public class TestModule extends AbstractModule {
  @Override
  protected void configure() {
    
  }

  @Provides
  @Singleton
  protected Configuration hibernateConfig() {
    Configuration config = new Configuration();

    config.addAnnotatedClass(App.class);
    config.addAnnotatedClass(Order.class);
    config.addAnnotatedClass(Targeting.class);
    config.addAnnotatedClass(BaseAnswer.class);
    config.addAnnotatedClass(BaseQuestion.class);
    config.addAnnotatedClass(Answer.class);
    config.addAnnotatedClass(Choice.class);
    config.addAnnotatedClass(Poll.class);
    config.addAnnotatedClass(Question.class);
    config.addAnnotatedClass(Vote.class);
    config.addAnnotatedClass(Account.class);
    config.addAnnotatedClass(AccountTx.class);
    config.addAnnotatedClass(Reservation.class);
    
    config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:heymoose");
    config.setProperty("hibernate.connection.username", "sa");
    config.setProperty("hibernate.connection.password", "");

    config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

    config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    config.setProperty("hibernate.show_sql", "true");
    config.setProperty("hibernate.format_sql", "false");
    config.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
    config.setProperty("hibernate.current_session_context_class", "thread");
    config.setProperty("hibernate.jdbc.batch_size", "0");
    return config;
  }

  @Provides
  @Singleton
  @Named("settings")
  protected Properties settings() {
    Properties settings = new Properties();
    settings.setProperty("question-cost", "15.0");
    return settings;
  }
}
