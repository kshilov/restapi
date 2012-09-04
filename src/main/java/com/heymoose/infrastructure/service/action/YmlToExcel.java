package com.heymoose.infrastructure.service.action;

import com.google.common.io.Closeables;
import com.google.common.io.OutputSupplier;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public final class YmlToExcel {

  private static final Logger log = LoggerFactory.getLogger(YmlToExcel.class);

  private final String[] HEADER = new String[] {
        "код", "название", "описание",
        "url", "изображение", "цена", "валюта",
        "экслюзивный", "вознаграждение" };

  public void doExport(YmlCatalogWrapper catalog,
                       OutputSupplier<? extends OutputStream> outputSupplier) {

    log.info("Starting export of yml catalog to XLS.");
    Workbook workbook = new HSSFWorkbook();
    CreationHelper creator = workbook.getCreationHelper();
    Sheet sheet = workbook.createSheet("items");
    int rows = 0;
    int cols = 0;
    Row header = sheet.createRow(rows++);
    for (String col : HEADER) {
      header.createCell(cols++).setCellValue(col);
    }

    for (Offer offer : catalog.listOffers()) {
      String code = catalog.getOfferCode(offer);
      String title = catalog.getOfferTitle(offer);
      log.info("Exporting offer: {} - {}.", code, title);
      cols = 0;
      Row offerRow = sheet.createRow(rows++);
      offerRow.createCell(cols++).setCellValue(code);
      offerRow.createCell(cols++).setCellValue(title);
      offerRow.createCell(cols++).setCellValue(offer.getDescription());
      offerRow.createCell(cols++).setCellValue(offer.getUrl());
      offerRow.createCell(cols++).setCellValue(offer.getPicture());
      offerRow.createCell(cols++).setCellValue(offer.getPrice());
      offerRow.createCell(cols++).setCellValue(offer.getCurrencyId());
      try {
        offerRow.createCell(cols++).setCellValue(catalog.isExclusive(offer));
      } catch (YmlCatalogWrapper.NoInfoException e) {
        offerRow.createCell(cols++).setCellValue(false);
      }
      try {
        switch (catalog.getCpaPolicy(offer)) {
          case PERCENT:
            String revenue = String.format("%s%%", catalog.getPercent(offer));
            offerRow.createCell(cols++).setCellValue(revenue);
            break;
          case FIXED:
            offerRow.createCell(cols++).setCellValue(
                catalog.getCost(offer).toString());
            break;
        }
      } catch (YmlCatalogWrapper.NoInfoException e) {
        log.warn("No info for offer: {} - {}. Skipping.", code, title);
      }
    }
    OutputStream out = null;
    try {
      out = outputSupplier.getOutput();
      workbook.write(out);
    } catch (IOException e) {
      log.error("Error writing xls.", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(out);
    }
    log.info("XLS export done.");
  }
}
