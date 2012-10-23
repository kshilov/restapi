package com.heymoose.infrastructure.service.yml;

import com.google.common.io.Closeables;
import com.google.common.io.OutputSupplier;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.infrastructure.persistence.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ProductExcelExporter {

  private static final Logger log =
      LoggerFactory.getLogger(ProductExcelExporter.class);

  private final Repo repo;

  @Inject
  public ProductExcelExporter(Repo repo) {
    this.repo = repo;
  }


  private static final String[] HEADER = new String[] {
      "код", "название", "описание",
      "url", "изображение", "цена", "валюта",
      "экслюзивный", "вознаграждение" };

  @Transactional
  public void doExport(Long parentOfferId,
                       OutputSupplier<? extends OutputStream> outputSupplier) {
    log.info("Starting export of yml catalog to XLS.");
    List<Product> productList = repo.allByHQL(Product.class,
        "from Product where offer.id = ?", parentOfferId);
    Workbook workbook = new HSSFWorkbook();
    Sheet sheet = workbook.createSheet("items");
    int rows = 0;
    int cols = 0;
    Row header = sheet.createRow(rows++);
    for (String col : HEADER) {
      header.createCell(cols++).setCellValue(col);
    }

    for (Product product : productList) {
      String code = product.originalId();
      String title = product.name();
      log.info("Exporting product: {} - {}.", code, title);
      cols = 0;
      Row offerRow = sheet.createRow(rows++);
      offerRow.createCell(cols++).setCellValue(code);
      offerRow.createCell(cols++).setCellValue(title);
      offerRow.createCell(cols++).setCellValue(
          product.attributeValue("description"));
      offerRow.createCell(cols++).setCellValue(product.url());
      offerRow.createCell(cols++).setCellValue(
          product.attributeValue("picture"));
      offerRow.createCell(cols++).setCellValue(product.price().toString());
      offerRow.createCell(cols++).setCellValue(
          product.attributeValue("currencyId"));
      offerRow.createCell(cols++).setCellValue(product.exclusive());
      switch (product.tariff().cpaPolicy()) {
        case PERCENT:
          String revenue = String.format("%s%%",
              product.tariff().affiliatePercent());
          offerRow.createCell(cols++).setCellValue(revenue);
          break;
        case FIXED:
          offerRow.createCell(cols++).setCellValue(
              product.tariff().affiliateCost().toString());
          break;
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
