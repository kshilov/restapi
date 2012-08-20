package com.heymoose.infrastructure.service.sapato;

import com.heymoose.infrastructure.util.QueryUtil;
import org.joda.time.DateTime;

import javax.inject.Provider;
import java.net.URL;

public final class SapatoUrlProvider implements Provider<URL> {

  private static final String START_TIME = "start-time";
  private static final int IMPORT_DAYS = 30;

  private final URL baseUrl;

  public SapatoUrlProvider(URL base) {
    this.baseUrl = base;
  }

  @Override
  public URL get() {
    DateTime startTime = DateTime.now().minusDays(IMPORT_DAYS);
    try {
      return QueryUtil.appendQueryParam(
          baseUrl.toURI(), START_TIME,
          startTime.toDate().getTime()).toURL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
