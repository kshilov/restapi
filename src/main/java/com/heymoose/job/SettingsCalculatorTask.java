package com.heymoose.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.domain.settings.Settings;
import com.heymoose.hibernate.Transactional;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SettingsCalculatorTask {

  private final static Logger log = LoggerFactory.getLogger(SettingsCalculatorTask.class);
  private final Provider<Session> sessionProvider;
  
  @Inject
  public SettingsCalculatorTask(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }
  
  private Session hiber() {
    return sessionProvider.get();
  }

  public void run(DateTime startTime) {
    log.info("Started at {}", DateTime.now());
    
    try {
      updateDavg();
    }
    catch (Exception e) {
      log.error("failed to update Davg");
    }
    
    log.info("Finished at {}", DateTime.now());
  }
  
  @Transactional
  public void updateDavg() {
    String sql = "update setting " +
        "set value = (select coalesce(round(avg(d), 2), '0.0') from app where deleted = false) " +
        "where name = :name";
    hiber().createSQLQuery(sql).setString("name", Settings.D_AVG).executeUpdate();
  }
}
