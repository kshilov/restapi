package com.heymoose.job;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.domain.TxType;
import com.heymoose.hibernate.Transactional;

@Singleton
public class UserStatCalculatorTask {

  private final static Logger log = LoggerFactory.getLogger(UserStatCalculatorTask.class);
  private final Provider<Session> sessionProvider;
  
  @Inject
  public UserStatCalculatorTask(Provider<Session> sessionProvider) {
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
    
    try { insertForNewUsers(); }
    catch (Exception e) { log.error("failed to create stats for new users"); }
    
    try { updatePayments(); }
    catch (Exception e) { log.error("failed to update users payments"); }
    
    try { updateUnpaidActions(); }
    catch (Exception e) { log.error("failed to update unpaid actions"); }
    
    log.info("Finished at {}", DateTime.now());
  }
  
  @Transactional
  public void insertForNewUsers() {
    executeSQL(
      "insert into user_stat (id, user_id) " +
      "select nextval('user_stat_seq'), id " +
      "from user_profile " +
      "where id not in (select user_id from user_stat)"
    );
  }
  
  @Transactional
  public void updatePayments() {
    executeSQL("update user_stat set payments = 0.00");
    
    String sql = "update user_stat " +
    	"set payments = user_payments.amount " +
    	"from (" +
    	"  select usr.id as user_id, sum(tx.diff) as amount " + 
      "  from user_profile usr join account_tx tx on usr.customer_account_id = tx.account_id " + 
      "  where usr.customer_account_id is not null and tx.type = :type " +
      "  group by usr.id " + 
    	") user_payments " +
    	"where user_stat.user_id = user_payments.user_id";
    hiber().createSQLQuery(sql).setInteger("type", TxType.REPLENISHMENT_ROBOKASSA.ordinal()).executeUpdate();
  }
  
  @Transactional
  public void updateUnpaidActions() {
    executeSQL("update user_stat set unpaid_actions = 0");
    executeSQL(
      "update user_stat " + 
  		"set unpaid_actions = user_actions.cnt " + 
  		"from ( " + 
  		"  select app.user_id as user_id, count(act.id) as cnt " + 
  		"  from app " + 
  		"  join action act on app.id = act.app_id " + 
  		"  join user_profile usr on usr.id = app.user_id " + 
  		"  where act.done = true  " + 
  		"  and act.creation_time > coalesce( " + 
  		"    (select timestamp from withdraw w  " + 
  		"      where w.account_id = usr.developer_account_id " + 
  		"      order by w.id desc limit 1), " + 
  		"    '1970-01-01') " + 
  		"  group by app.user_id " + 
  		") user_actions " + 
  		"where user_stat.user_id = user_actions.user_id"
    );
  }
}
