package com.heymoose.infrastructure.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.carolines.CarolinesRater;
import com.heymoose.infrastructure.service.mebelrama.MebelramaRater;
import com.heymoose.infrastructure.service.shoesbags.ShoesBagsRater;
import com.heymoose.infrastructure.service.topshop.TopShopRater;
import com.heymoose.infrastructure.service.trendsbrands.TrendsBrandsRater;
import com.heymoose.infrastructure.service.yml.MapRater;
import com.heymoose.infrastructure.service.yml.ProductExcelExporter;
import com.heymoose.infrastructure.service.yml.ProductYmlImporter;
import com.heymoose.infrastructure.service.yml.YmlBasedRater;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public final class YmlImport {
  private final static class Args {

    private enum Rater {
      DEFAULT, YML, TOPSHOP, TRENDSBRANDS, CAROLINES, SHOESBAGS,
      MEBELRAMA
    }

    @Parameter(description = ".yml file for importing.", required = true)
    private List<String> ymlPath;

    @Parameter(names = "-offer", description = "id of parent offer.",
        required = true)
    private Long offerId;

    @Parameter(names = "-csv",
        description = ".csv file with (item id)-(price) mappings.")
    private String csvPath;

    @Parameter(names = "-percent",
        description = "default percent for non-exclusive items.")
    private BigDecimal defaultPercent;

    @Parameter(names = "-rater", description =  "custom rater.")
    private Rater rater = Rater.DEFAULT;

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help;

    @Parameter(names = "--import", description = "do import data to db")
    private boolean doImport = false;

    @Parameter(names = "--export", description = "do export data to xls")
    private boolean doExport = false;

    @Override
    public String toString() {
      return Objects.toStringHelper("")
          .add("yml", ymlPath)
          .add("csv", csvPath)
          .add("default percent", defaultPercent)
          .add("offer", offerId)
          .toString();
    }
  }

  private static final Charset UTF = Charset.forName("utf8");
  private static final Logger log = LoggerFactory.getLogger(YmlImport.class);


  public static void main(String... args) throws Exception {
    final Args arguments = new Args();
    JCommander jc = new JCommander(arguments, args);
    jc.setProgramName("ymlimport");

    if (arguments.help) {
      jc.usage();
      return;
    }


    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule());

    String ymlPath = arguments.ymlPath.get(0);
    URL url;
    if (!ymlPath.contains("://")) {
      url = Resources.getResource(ymlPath);
    } else {
      url = new URL(ymlPath);
    }
    InputSupplier<InputStream> inputSupplier =
        Resources.newInputStreamSupplier(url);

    Repo repo = injector.getInstance(Repo.class);
    ProductRater rater = null;
    switch (arguments.rater) {
      case DEFAULT:
        rater = new MapRater(
            parseCsv(arguments.csvPath),
            arguments.defaultPercent);
        break;
      case YML:
        rater = new YmlBasedRater();
        break;
      case TOPSHOP:
        rater = new TopShopRater();
        break;
      case TRENDSBRANDS:
        rater = new TrendsBrandsRater();
        break;
      case CAROLINES:
        rater = new CarolinesRater();
        break;
      case SHOESBAGS:
        rater = new ShoesBagsRater();
        break;
      case MEBELRAMA:
        rater = new MebelramaRater();
        break;
    }
    log.info("Rater chosen: {}", rater.getClass().getSimpleName());

    List<Product> productList = ImmutableList.of();
    if (arguments.doImport) {
      log.info("** Starting import with arguments: {} **", arguments);
      ProductYmlImporter importer =
          injector.getInstance(ProductYmlImporter.class);
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(inputSupplier.getInput());
      productList = importer.doImport(document, arguments.offerId, rater);
    }
    if (arguments.doExport) {
      log.info("Starting export to XLS.");
      if (productList.isEmpty())
        productList = repo.allByHQL(Product.class,
            "from Product where offer.id = ?",
            arguments.offerId);
      ProductExcelExporter exporter = new ProductExcelExporter();
      File xls = new File(arguments.offerId + ".xls");
      exporter.doExport(productList, Files.newOutputStreamSupplier(xls));
    }
  }

  private static Map<String, BigDecimal> parseCsv(String csvPath) {
    if (Strings.isNullOrEmpty(csvPath))
      return ImmutableMap.of();
    final ImmutableMap.Builder<String, BigDecimal> idPercentMap =
        ImmutableMap.builder();
    try {
      for (String line : Files.readLines(new File(csvPath), UTF)) {
        String[] splitted = line.split(",");
        idPercentMap.put(splitted[0], new BigDecimal(splitted[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return idPercentMap.build();
  }

}
