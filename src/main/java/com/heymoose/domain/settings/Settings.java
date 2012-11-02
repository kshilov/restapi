package com.heymoose.domain.settings;

import com.heymoose.infrastructure.persistence.Transactional;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Singleton
public class Settings {

  public static final String M = "M";
  public static final String Q = "Q";
  public static final String C_MIN = "Cmin";

  public static final String REFERRAL_OFFER = "referral-offer";

  private final Provider<Session> sessionProvider;
  
  @Inject
  public Settings(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
    init();
  }

  @Transactional
  public void init() {
    saveIfNotExists(M, "0.1");
    saveIfNotExists(Q, "1.1");
    saveIfNotExists(C_MIN, "2.0");
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
  public long getLong(String name) {
    checkNotNull(name);
    return Long.valueOf(existingSetting(name).value);
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

  public BigDecimal M() {
    return new BigDecimal(getDouble(Settings.M));
  }
  
  public BigDecimal Cmin() {
    return new BigDecimal(getDouble(Settings.C_MIN));
  }
}
