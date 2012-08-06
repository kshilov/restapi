package com.heymoose.infrastructure.server;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.delikateska.DelikateskaYmlImporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

public final class DelikateskaYmlImport {
  private static final Charset UTF = Charset.forName("utf8");

  public static void main(String... args) throws IOException {
    if (Strings.isNullOrEmpty(args[0])) {
      System.err.println("YML file not specified.");
      System.exit(2);
    }
    if (Strings.isNullOrEmpty(args[1])) {
      System.err.println("id-percent.csv not specified.");
      System.exit(2);
    }

    final ImmutableMap.Builder<Integer, Integer> idPercentMap =
        ImmutableMap.builder();
    for (String line : Files.readLines(new File(args[1]), UTF)) {
      String[] splitted = line.split(",");
      idPercentMap.put(Integer.valueOf(splitted[0]), Integer.valueOf(splitted[1]));
    }

    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Key.get(
                new TypeLiteral<Map<Integer, Integer>>() { },
                Names.named("id-percent-map"))).toInstance(idPercentMap.build());
          }
        });
    Properties properties = injector.getInstance(
        Key.get(Properties.class, Names.named("settings")));
    Long parentOfferId = Long.valueOf(
        properties.get("delikateska.offer").toString());
    File file = new File(args[0]);
    InputSupplier<FileInputStream> inputSupplier =
        Files.newInputStreamSupplier(file);

    DelikateskaYmlImporter importer =
        injector.getInstance(DelikateskaYmlImporter.class);

    importer.doImport(inputSupplier, parentOfferId);
  }
}
