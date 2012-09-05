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
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.action.PercentPerItemYmlWrapper;
import com.heymoose.infrastructure.service.action.YmlImporter;
import com.heymoose.infrastructure.service.action.YmlToExcel;
import com.heymoose.infrastructure.service.carolines.CarolinesYmlWrapper;
import com.heymoose.infrastructure.service.shoesbags.ShoesBagsYmlWrapper;
import com.heymoose.infrastructure.service.topshop.TopShopYmlWrapper;
import com.heymoose.infrastructure.service.trendsbrands.TrendsBrandsYmlWrapper;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapper;
import com.heymoose.infrastructure.service.yml.YmlUtil;
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

    private enum Wrapper {
      DEFAULT, TOPSHOP, TRENDSBRANDS, CAROLINES, SHOESBAGS
    }

    @Parameter(description = ".yml file for importing.", required = true)
    private List<String> ymlPath;

    @Parameter(names = "-offer", description = "id of parent offer.", required = true)
    private Long offerId;

    @Parameter(names = "-csv",
        description = ".csv file with (item id)-(price) mappings.")
    private String csvPath;

    @Parameter(names = "-percent",
        description = "default percent for non-exclusive items.")
    private BigDecimal defaultPercent;

    @Parameter(names = "-wrapper", description =  "custom wrapper.")
    private Wrapper wrapper = Wrapper.DEFAULT;

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help;

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
    if (!ymlPath.contains("://")) {
      ymlPath = "file://" + ymlPath;
    }
    InputSupplier<InputStream> inputSupplier =
        Resources.newInputStreamSupplier(new URL(ymlPath));

    Repo repo = injector.getInstance(Repo.class);
    YmlCatalog catalog = YmlUtil.parse(inputSupplier);
    YmlCatalogWrapper wrapper = null;
    switch (arguments.wrapper) {
      case DEFAULT:
        wrapper = new PercentPerItemYmlWrapper(
            catalog,
            arguments.defaultPercent,
            parseCsv(arguments.csvPath));
        break;
      case TOPSHOP:
        wrapper = new TopShopYmlWrapper(catalog);
        break;
      case TRENDSBRANDS:
        wrapper = new TrendsBrandsYmlWrapper(catalog);
        break;
      case CAROLINES:
        wrapper = new CarolinesYmlWrapper(catalog);
        break;
      case SHOESBAGS:
        wrapper = new ShoesBagsYmlWrapper(catalog);
        break;
    }
    if (wrapper == null) {
      log.error("Wrapper not found. Arguments: {}", arguments);
      return;
    }
    log.info("Wrapper chosen: {}", wrapper.getClass().getSimpleName());
    log.info("** Starting import with arguments: {} **", arguments);
    YmlImporter importer = new YmlImporter(repo);
    importer.doImport(wrapper, arguments.offerId);
    log.info("Starting export to XLS.");
    YmlToExcel exporter = new YmlToExcel();
    exporter.doExport(
        wrapper, Files.newOutputStreamSupplier(new File("yml.xls")));
  }

  private static Map<String, BigDecimal> parseCsv(String csvPath) {
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
