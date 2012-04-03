package com.heymoose.test.base;

import com.google.inject.Injector;
import com.heymoose.server.Launcher;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class RestTest {

  protected final static Logger log = LoggerFactory.getLogger(RestTest.class);

  private final static int TEST_PORT = 5467;
  private static Injector injector;

  protected RestTest() {
    try {
      Launcher.launch(TEST_PORT, TestContextListener.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String baseUrl() {
    return "http://127.0.0.1:" + TEST_PORT;
  }

  protected WebResource client() {
    Client client = Client.create();
    client.setFollowRedirects(false);
    return client.resource(baseUrl());
  }

  protected Heymoose heymoose() {
    return new Heymoose(client());
  }

  protected Injector injector() {
    return TestContextListener.injector();
  }

  protected void sqlUpdate(String sql) {
    SessionFactory sessionFactory = injector().getInstance(SessionFactory.class);
    Session session = sessionFactory.openSession();
    Transaction tx =  session.beginTransaction();
    try {
      session.createSQLQuery(sql).executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
      tx.rollback();
    }
    session.close();
  }

  @Before public void reset() throws InterruptedException {
    SessionFactory sessionFactory = injector().getInstance(SessionFactory.class);
    Session session = sessionFactory.openSession();
    Transaction tx =  session.beginTransaction();
    try {
      session.createQuery("delete from ClickStat").executeUpdate();
      session.createQuery("delete from ShowStat").executeUpdate();
      session.createQuery("delete from OfferGrant").executeUpdate();
      session.createQuery("delete from Banner").executeUpdate();
      session.createSQLQuery("delete from offer_category").executeUpdate();
      session.createSQLQuery("delete from offer_region").executeUpdate();
      session.createQuery("delete from BaseOffer").executeUpdate();
      session.createSQLQuery("delete from user_role").executeUpdate();
      session.createQuery("delete from User").executeUpdate();
      session.createQuery("delete from Category").executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
      tx.rollback();
    }
    session.close();
  }
}
