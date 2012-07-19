package com.heymoose.infrastructure.server;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.heymoose.domain.topshop.TopShopProduct;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;

public class TopShopImport {

  private static class TopShopOffersModule extends AbstractModule {

    @Override
    protected void configure() {
    }

  }

  private static Logger log = LoggerFactory.getLogger(TopShopImport.class);

  public static void main(String... args) throws Exception {
    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule(),
        new TopShopOffersModule());
    if (Strings.isNullOrEmpty(args[0])) {
      System.out.println("YML file not specified.");
      System.exit(2);
    }
    JAXBContext context = JAXBContext.newInstance(YmlCatalog.class);
    Reader reader = Files.newReader(new File(args[0]), Charset.forName("utf8"));
    Unmarshaller unmarshaller = context.createUnmarshaller();
    YmlCatalog cat = (YmlCatalog) unmarshaller.unmarshal(reader);
    log.info("{} products found.", cat.getShop().getOffers().getOffer().size());

    Session session = injector.getInstance(Session.class);
    Transaction tx = session.beginTransaction();
    try {
      for (Offer catalogOffer : cat.getShop().getOffers().getOffer()) {
        TopShopProduct product = new TopShopProduct(
            Long.valueOf(catalogOffer.getId()));
        product.setPrice(new BigDecimal(catalogOffer.getPrice()));
        session.saveOrUpdate(product);
      }
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
    }
  }
}
