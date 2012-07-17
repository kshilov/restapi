package com.heymoose.infrastructure.service.topshop;

import com.google.common.io.InputSupplier;
import com.heymoose.infrastructure.job.Job;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public final class TopShopImportJob implements Job {

  private static final Logger log =
      LoggerFactory.getLogger(TopShopImportJob.class);

  private final URL url;
  private final TopShopDataImporter importer;
  private final TopShopXmlConverter converter = new TopShopXmlConverter();

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
    final InputStream input = connection.getInputStream();
    List<TopShopPaymentData> converted =
        converter.convert(new InputSupplier<InputStream>() {
      @Override
      public InputStream getInput() throws IOException {
        return input;
      }
    });
    importer.doImport(converted);
  }

}
