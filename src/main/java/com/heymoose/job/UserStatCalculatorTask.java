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

  private final static Logger log = LoggerFactory.getLogger(SettingsCalculatorTask.class);
  private final Provider<Session> sessionProvider;
  
  @Inject
  public UserStatCalculatorTask(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }
  
  private Session hiber() {
    return sessionProvider.get();
  }

  public void run(DateTime startTime) {
    log.info("Started at {}", DateTime.now());
    
    try {
      calcStats();
    }
    catch (Exception e) {
      log.error("failed to calculate user stats");
    }
    
    log.info("Finished at {}", DateTime.now());
  }
  
  @Transactional
  public void calcStats() {
    String sql = "delete from user_stat";    
    hiber().createSQLQuery(sql).executeUpdate();
    
    sql = "insert into user_stat (id, user_id, payments) " + 
  		"select nextval('user_stat_seq'), user_id, sum(payments) " + 
  		"from (" + 
  		"  select id as user_id, 0.00 as payments from user_profile " + 
  		"  union " + 
  		"  select usr.id as user_id, tx.diff as payments " + 
  		"  from user_profile usr join account_tx tx on usr.customer_account_id = tx.account_id " + 
  		"  where usr.customer_account_id is not null and tx.type = :type " + 
  		") _a " + 
  		"group by user_id";
    hiber().createSQLQuery(sql).setInteger("type", TxType.REPLENISHMENT_ROBOKASSA.ordinal()).executeUpdate();
  }
}
