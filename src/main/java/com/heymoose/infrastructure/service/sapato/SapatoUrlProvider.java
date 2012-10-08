package com.heymoose.infrastructure.service.sapato;

import com.heymoose.infrastructure.util.QueryUtil;
import org.joda.time.DateTime;

import javax.inject.Provider;
import java.net.MalformedURLException;
import java.net.URL;

public final class SapatoUrlProvider implements Provider<URL> {

  private static final String START_TIME = "start-time";
  private static final int IMPORT_DAYS = 30;

  private final URL baseUrl;

  public SapatoUrlProvider(URL base) {
    this.baseUrl = base;
  }

  public SapatoUrlProvider(String base) {
    try {
      this.baseUrl = new URL(base);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public URL get() {
    DateTime startTime = DateTime.now().minusDays(IMPORT_DAYS);
    long startTimeSeconds = Math.round(startTime.getMillis() / 1000.0);
    try {
      return QueryUtil.appendQueryParam(
          baseUrl.toURI(), START_TIME,
          startTimeSeconds).toURL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
