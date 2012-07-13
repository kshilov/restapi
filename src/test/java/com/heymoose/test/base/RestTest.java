package com.heymoose.test.base;

import com.google.inject.Injector;
import com.heymoose.infrastructure.server.Launcher;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Ignore
public class RestTest {

  protected final static Logger log = LoggerFactory.getLogger(RestTest.class);

  private final static int TEST_PORT = 5467;

  protected RestTest() {
  }

  @BeforeClass
  public static void start() {
    try {
      Launcher.launch(TEST_PORT, TestContextListener.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static String baseUrl() {
    return "http://127.0.0.1:" + TEST_PORT;
  }

  protected static WebResource client() {
    Client client = Client.create();
    client.setFollowRedirects(false);
    return client.resource(baseUrl());
  }

  protected static Heymoose heymoose() {
    return new Heymoose(client());
  }

  protected static Injector injector() {
    return TestContextListener.injector();
  }

  protected static void sqlUpdate(String sql) {
    SessionFactory sessionFactory = injector().getInstance(SessionFactory.class);
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      session.createSQLQuery(sql).executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
      tx.rollback();
    }
    session.close();
  }

  @SuppressWarnings("unchecked")
  protected static <T> List<T> select(Class<T> cls,
                                          Criterion... criterionList) {
    SessionFactory sessionFactory = injector()
        .getInstance(SessionFactory.class);
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Criteria criteria = session.createCriteria(cls);
      for (Criterion criterion : criterionList) {
        criteria.add(criterion);
      }
      List<T> result = criteria.list();
      tx.commit();
      return result;
    } catch (Throwable e) {
      tx.rollback();
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }

  public static void reset() {
    SessionFactory sessionFactory = injector().getInstance(SessionFactory.class);
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      session.createQuery("delete from OfferAction").executeUpdate();
      session.createQuery("delete from Token").executeUpdate();
      session.createQuery("delete from OfferStat").executeUpdate();
      session.createQuery("delete from OfferGrant").executeUpdate();
      session.createQuery("delete from Banner").executeUpdate();
      session.createSQLQuery("delete from offer_category").executeUpdate();
      session.createSQLQuery("delete from offer_region").executeUpdate();
      session.createQuery("delete from BaseOffer").executeUpdate();
      session.createSQLQuery("delete from user_role").executeUpdate();
      session.createQuery("delete from User").executeUpdate();
      session.createQuery("delete from Category").executeUpdate();
      session.createQuery("delete from CategoryGroup").executeUpdate();
      session.createSQLQuery("delete from ip_segment").executeUpdate();
      session.createSQLQuery("delete from error_info").executeUpdate();
      session.createQuery("delete from Withdraw").executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
      tx.rollback();
    }
    session.close();
  }
}
