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
  private final Long parentOfferId;

  public ActionImportJob(String url, Long parentOffer,
                         ActionDataImporter importer,
                         ActionParser parser) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.importer = importer;
    this.parser = parser;
    this.parentOfferId = parentOffer;
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
      importer.doImport(converted, parentOfferId);
    } catch (Throwable e) {
      String msg = String.format("Exception occurred during data import " +
          "for offer: %s, from url: %s", parentOfferId, url);
      log.error(msg, e);
    }
  }
}
