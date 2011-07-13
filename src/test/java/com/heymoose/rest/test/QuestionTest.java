package com.heymoose.rest.test;

import com.heymoose.rest.domain.account.AccountTx;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Orders;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.poll.Answer;
import com.heymoose.rest.domain.poll.Question;
import com.heymoose.rest.test.base.ApiTest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class QuestionTest extends ApiTest {

  private final static Logger log = LoggerFactory.getLogger(QuestionTest.class);

  @Test public void create() {
    Configuration cfg = injector().getInstance(Configuration.class);
    SchemaExport e = new SchemaExport(cfg);

    e.setOutputFile("/tmp/schema.sql");
    e.create(true, false);

    SessionFactory sessionFactory = injector().getInstance(SessionFactory.class);

    Session session = sessionFactory.getCurrentSession();
    session.beginTransaction();


    Targeting t = new Targeting(20, true, "Moscow", "Russia");
    Order order = new Order(new BigDecimal("10.0"), "O1", t, new BigDecimal("5.0"));
    session.save(order);

    log.info("account before: {}", order.account().balance().toString());

    Question q = new Question("Some q", order);
    q.ask();
    session.save(q);
    Answer a = new Answer(q, "RTFM!!!");
    session.save(a);
    a = new Answer(q, "RTFM!!!2");
    session.save(a);
    a = new Answer(q, "RTFM!!!3");
    session.save(a);
    a = new Answer(q, "RTFM!!!4");
    session.save(a);
    a = new Answer(q, "RTFM!!!5");
    session.save(a);
    a = new Answer(q, "RTFM!!!6");    
    session.save(a);

    Orders p = injector().getInstance(Orders.class);
    p.maybeExecute(q);

    log.info("account after: {}", order.account().balance().toString());

    List<AccountTx> txx = session.createQuery("from AccountTx").list();
    for (AccountTx tx : txx) {
      log.info("************************************");
      log.info("id: {}", tx.id());
      log.info("account: {}", tx.balance().toString());
      log.info("parentId: {}", tx.parentId());
      log.info("version: {}", tx.version());
      log.info("description: {}", tx.description());
      log.info("diff: {}", tx.diff());
    }

    session.getTransaction().commit();
  }
}
