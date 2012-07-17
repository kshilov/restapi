package com.heymoose.infrastructure.service.topshop;

import com.heymoose.infrastructure.job.Job;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public final class TopShopImportJob implements Job {

  private static final Logger log =
      LoggerFactory.getLogger(TopShopImportJob.class);

  private final URL url;
  private final TopShopDataImporter importer;

  public TopShopImportJob(String url, TopShopDataImporter importer) {
    this.importer = importer;
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run(DateTime plannedStartTime) throws Exception {
    URLConnection connection = url.openConnection();
    log.info("{}", connection.getContent());
  }

}
