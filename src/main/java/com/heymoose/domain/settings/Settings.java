package com.heymoose.domain.settings;

import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class Settings {

  private final Provider<Session> sessionProvider;
  
  public static String M = "M";
  public static String Q = "Q";
  public static String D_AVG = "Davg";

  @Inject
  public Settings(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
    init();
  }

  @Transactional
  public void init() {
    saveIfNotExists(M, "0.1");
    saveIfNotExists(Q, "1.1");
    saveIfNotExists(D_AVG, "1.9");
  }

  @Transactional
  private void saveIfNotExists(String name, String value) {
    Session s = sessionProvider.get();
    Setting existing = (Setting) s.get(Setting.class, name);
    if (existing == null)
      s.save(new Setting(name, value));
  }

  @Transactional
  public void set(String name, Object value) {
    checkNotNull(name, value);
    Setting setting = existingSetting(name);
    setting.value = value.toString();
  }

  @Transactional
  public String getString(String name) {
    checkNotNull(name);
    Setting setting = existingSetting(name);
    return setting.value;
  }

  @Transactional
  public int getInt(String name) {
    checkNotNull(name);
    Setting setting = existingSetting(name);
    return Integer.valueOf(setting.value);
  }

  @Transactional
  public double getDouble(String name) {
    checkNotNull(name);
    Setting setting = existingSetting(name);
    return Double.valueOf(setting.value);
  }

  @Transactional
  public Iterable<Setting> list() {
    return sessionProvider.get().createQuery("from Setting").list();
  }
  
  public Map<String, String> map() {
    Map<String, String> settingsMap = newHashMap();
    Iterable<Setting> settings = list();
    for (Setting setting : settings)
      settingsMap.put(setting.name, setting.value);
    return settingsMap;
  }

  private Setting existingSetting(String name) {
    Setting setting = (Setting) sessionProvider.get().get(Setting.class, name);
    if (setting == null)
      throw new IllegalArgumentException("Unknown setting " + name);
    return setting;
  }

  @Transactional
  public BigDecimal Cmin() {
    double Davg = getDouble(Settings.D_AVG);
    double M = getDouble(Settings.M);
    return new BigDecimal(Davg + M);
  }

  public BigDecimal M() {
    return new BigDecimal(getDouble(Settings.M));
  }
}
