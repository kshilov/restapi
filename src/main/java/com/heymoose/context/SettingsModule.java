package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SettingsModule extends AbstractModule {

  private final static String SETTING_PROPERTY = "settingsFile";

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  @Named("settings")
  protected Properties settings() throws IOException {
    Properties settings = new Properties();
    String settingFile = System.getProperty(SETTING_PROPERTY);
    InputStream inputStream = new FileInputStream(settingFile);
    settings.load(inputStream);
    inputStream.close();
    return settings;
  }
}
