package com.heymoose.job;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;

@Singleton
public class AppStatCalculatorTask {

  private final static Logger log = LoggerFactory.getLogger(AppStatCalculatorTask.class);
  private final Provider<Session> sessionProvider;
  
  @Inject
  public AppStatCalculatorTask(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }
  
  private Session hiber() {
    return sessionProvider.get();
  }
  
  private int executeSQL(String sql) {
    return hiber().createSQLQuery(sql).executeUpdate();
  }

  public void run(DateTime startTime, boolean allWeek) {
    log.info("Started at {}", DateTime.now());
    
    try { insertForNewApps(); }
    catch (Exception e) { log.error("failed to create stats for new apps"); }
    
    try { updateShowsOverall(); }
    catch (Exception e) { log.error("failed to update overall shows count"); }
    
    try { updateActionsOverall(); }
    catch (Exception e) { log.error("failed to update overall actions count"); }
    
    try { updateAverageDau(); }
    catch (Exception e) { log.error("failed to update average DAU"); }
    
    int dayFinish = allWeek ? 6 : 0;
    for (int i = 0; i <= dayFinish; ++i) {
      try { updateDau(i); }
      catch (Exception e) { log.error(String.format("failed to update DAU for %d days ago", i+1)); }
    }
    
    log.info("Finished at {}", DateTime.now());
  }
  
  @Transactional
  public void insertForNewApps() {
    executeSQL(
      "insert into app_stat (id, app_id) " +
      "select nextval('app_stat_seq'), id " +
      "from app " +
      "where id not in (select app_id from app_stat)"
    );
  }
  
  @Transactional
  public void updateShowsOverall() {
    executeSQL("update app_stat set shows_overall = 0");
    executeSQL(
      "update app_stat " +
  		"set shows_overall = app_shows.cnt " +
  		"from (select app_id, count(id) as cnt from offer_show group by app_id) app_shows " +
  		"where app_stat.app_id = app_shows.app_id"
  	);
  }
  
  @Transactional
  public void updateActionsOverall() {
    executeSQL("update app_stat set actions_overall = 0");
    executeSQL(
      "update app_stat " +
      "set actions_overall = app_actions.cnt " +
      "from (select app_id, count(id) as cnt from action where done = true group by app_id) app_actions " +
      "where app_stat.app_id = app_actions.app_id"
    );
  }
  
  @Transactional
  public void updateAverageDau() {
    executeSQL("update app_stat set dau_average = 0.00");
    executeSQL(
      "update app_stat " + 
      "set dau_average = app_dau.dau " + 
      "from ( " + 
      "  select app_id, count(distinct performer_id) / 7.0 as dau " + 
      "  from offer_show " + 
      "  where show_time >= now() - CAST('1 WEEK' AS interval) " + 
      "  group by app_id " + 
      ") app_dau " + 
      "where app_stat.app_id = app_dau.app_id"
    );
  }
  
  @Transactional
  public void updateDau(int daysOffset) {
    int dayOfWeek = DateTime.now().minusDays(1 + daysOffset).getDayOfWeek() % 7;
    String exprOffset = daysOffset > 0 ? String.format("- CAST('%d DAY' AS interval)", daysOffset) : "";
    
    executeSQL(String.format("update app_stat set dau_day%d = 0.00", dayOfWeek));
    executeSQL(String.format(
      "update app_stat " + 
      "set dau_day%d = app_dau.dau " + 
      "from ( " + 
      "  select app_id, count(distinct performer_id) as dau " + 
      "  from offer_show " + 
      "  where show_time between date_trunc('day', now()) - CAST('1 DAY' AS interval) %s " +
      "    and date_trunc('day', now()) %s " + 
      "  group by app_id " + 
      ") app_dau " + 
      "where app_stat.app_id = app_dau.app_id",
      dayOfWeek, exprOffset, exprOffset
    ));
  }
}