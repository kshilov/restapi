package com.heymoose.infrastructure.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.yml.ProductYmlImporter;
import com.heymoose.infrastructure.service.yml.YmlBasedRater;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import java.io.File;

public final class ProductImport {


  public static void main(String... args) throws Exception {
    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule());
    SAXBuilder builder = new SAXBuilder();
    Document document = builder.build(new File(args[0]));
    injector.getInstance(ProductYmlImporter.class)
        .doImport(document, Long.valueOf(args[1]), new YmlBasedRater());
  }
}
