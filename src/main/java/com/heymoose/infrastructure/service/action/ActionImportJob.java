package com.heymoose.infrastructure.service.action;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public final class ActionImportJob implements Runnable {

  private static final Logger log =
      LoggerFactory.getLogger(ActionImportJob.class);

  private final URL url;
  private final ActionDataImporter importer;
  private final ActionParser parser;

  public ActionImportJob(String url, ActionDataImporter importer,
                         ActionParser parser) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.importer = importer;
    this.parser = parser;
  }

  @Override
  public void run() {
    try {
      log.info("** Import started from url: '{}' **", url);
      URLConnection connection = url.openConnection();
      final InputStream input = connection.getInputStream();
      List<ActionData> converted =
          parser.parse(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
              return input;
            }
          });
      importer.doImport(converted);
    } catch (Throwable e) {
      log.error("Exception occurred during data import.", e);
    }
  }
}
