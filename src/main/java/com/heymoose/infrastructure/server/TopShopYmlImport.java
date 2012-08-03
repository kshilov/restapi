package com.heymoose.infrastructure.server;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.topshop.TopShopYmlImporter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class TopShopYmlImport {

  public static void main(String... args) throws Exception {
    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule());
    if (Strings.isNullOrEmpty(args[0])) {
      System.out.println("YML file not specified.");
      System.exit(2);
    }
    Properties properties = injector.getInstance(
        Key.get(Properties.class, Names.named("settings")));
    Long parentOfferId = Long.valueOf(
        properties.get("topshop.offer").toString());
    File file = new File(args[0]);
    InputSupplier<FileInputStream> inputSupplier =
        Files.newInputStreamSupplier(file);
    injector.getInstance(TopShopYmlImporter.class).doImport(
        inputSupplier, parentOfferId);
  }
}
