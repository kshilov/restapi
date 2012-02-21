package com.heymoose.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OfferStatCalculatorTask {

  private final static Logger log = LoggerFactory.getLogger(OfferStatCalculatorTask.class);
  private final Provider<Session> sessionProvider;
  
  @Inject
  public OfferStatCalculatorTask(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }
  
  private Session hiber() {
    return sessionProvider.get();
  }
  
  private int executeSQL(String sql) {
    return hiber().createSQLQuery(sql).executeUpdate();
  }

  public void run(DateTime startTime) {
    log.info("Started at {}", DateTime.now());
    
    try { insertForNewOffers(); }
    catch (Exception e) { log.error("failed to create stats for new offers"); }
    
    try { updateShowsOverall(); }
    catch (Exception e) { log.error("failed to update overall shows count"); }
    
    try { updateActionsOverall(); }
    catch (Exception e) { log.error("failed to update overall actions count"); }
    
    log.info("Finished at {}", DateTime.now());
  }
  
  @Transactional
  public void insertForNewOffers() {
    executeSQL(
      "insert into offer_stat (id, offer_id) " +
      "select nextval('offer_stat_seq'), id " +
      "from offer " +
      "where id not in (select offer_id from offer_stat)"
    );
  }
  
  @Transactional
  public void updateShowsOverall() {
    executeSQL("update offer_stat set shows_overall = 0");
    executeSQL(
      "update offer_stat " +
      "set shows_overall = offer_shows.cnt " +
      "from (select offer_id, count(id) as cnt from offer_show group by offer_id) offer_shows " +
      "where offer_stat.offer_id = offer_shows.offer_id"
    );
  }
  
  @Transactional
  public void updateActionsOverall() {
    executeSQL("update offer_stat set actions_overall = 0");
    executeSQL(
      "update offer_stat " +
      "set actions_overall = offer_actions.cnt " +
      "from (select offer_id, count(id) as cnt from action where done = true group by offer_id) offer_actions " +
      "where offer_stat.offer_id = offer_actions.offer_id"
    );
  }
}