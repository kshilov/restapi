package com.heymoose.infrastructure.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.action.PercentPerItemYmlImporter;
import com.heymoose.infrastructure.service.yml.YmlImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public final class YmlImport {
  private final static class Args {

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

    File file = new File(arguments.ymlPath.get(0));
    InputSupplier<FileInputStream> inputSupplier =
        Files.newInputStreamSupplier(file);

    log.info("** Starting import with arguments: {} **", arguments);
    Repo repo = injector.getInstance(Repo.class);
    YmlImporter importer = new PercentPerItemYmlImporter(
        repo,
        arguments.defaultPercent,
        parseCsv(arguments.csvPath));
    importer.doImport(inputSupplier, arguments.offerId);
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
