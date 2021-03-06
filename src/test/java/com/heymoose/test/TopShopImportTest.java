package com.heymoose.test;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.service.Tariffs;
import com.heymoose.infrastructure.service.action.ItemListProductImporter;
import com.heymoose.infrastructure.service.topshop.TopShopActionParser;
import com.heymoose.test.base.RestTest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class TopShopImportTest extends RestTest {


  private static final Logger log =
      LoggerFactory.getLogger(TopShopImportTest.class);

  private final static double ADV_BALANCE = 100.0;
  private final static double OFFER_BALANCE = 70.0;
  private final static double PERCENT = 20.0;

  @Before
  public void before() {
    reset();
  }


  @Test
  public void importSinglePaymentTest() throws Exception {
    long advId = heymoose().doRegisterAdvertiser(ADV_BALANCE);
    long affId = heymoose().doRegisterAffiliate();
    long offerId = heymoose().doCreateCpaOffer(
        CpaPolicy.PERCENT, 0.0, PERCENT,
        OFFER_BALANCE, advId, "code");
    heymoose().doCreateGrant(offerId, affId);
    heymoose().doClick(offerId, affId, null, Subs.empty(), null);
    Token token = select(Token.class).get(0);
    log.info("Token id: {}", token.id());

    Long itemCode = 123L;
    String txId = "top-shop-order-id";
    Double price = 100.0;
    Long productSubOfferId = null;
    Repo repo = injector().getInstance(Repo.class);
    ItemListProductImporter importer = injector()
        .getInstance(ItemListProductImporter.class);
    TopShopActionParser converter = new TopShopActionParser();
    String topShopXml =
        "<payment_list>" +
          "<payment>" +
            "<order_id>" + txId + "</order_id>" +
            "<key>http://anyurl.com?_hm_token=" + token.value() + "</key>" +
            "<items>" +
              "<item>" + itemCode.toString() + "</item>" +
            "</items>" +
            "<status>20</status>" +
          "</payment>" +
        "</payment_list>";
    // manually start transaction, because not using guice
    Session session = injector().getProvider(Session.class).get();
    Transaction tx = session.beginTransaction();
    Product product = null;
    try {
      Offer parentOffer = repo.get(Offer.class, offerId);
      product = new Product()
          .setName("name")
          .setPrice(new BigDecimal(price))
          .setUrl("http://url.com")
          .setOffer(parentOffer)
          .setOriginalId(itemCode.toString());
      Tariffs tariffs = new Tariffs(repo);
      Tariff tariff = tariffs.createIfNotExists(CpaPolicy.PERCENT,
          new BigDecimal(PERCENT), parentOffer);
      product.setTariff(tariff);
      repo.put(product);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      throw e;
    }

    session = injector().getProvider(Session.class).get();
    tx = session.beginTransaction();
    try {
      List<ItemListActionData> data = converter.parse(input(topShopXml));
      importer.doImport(data, offerId);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      throw e;
    }

    OfferAction createdAction = select(OfferAction.class).get(0);
    log.info("Action created, id: {}", createdAction.id());
    OfferStat offerStat = select(OfferStat.class).get(1);
    log.info("Revenue: {}", offerStat.notConfirmedRevenue());
    User affiliate = select(User.class, Restrictions.eq("id", affId)).get(0);
    BigDecimal cost = new BigDecimal(price * PERCENT / 100.0);
    BigDecimal expectedRevenue = cost.divide(
        new BigDecimal((100 + product.tariff().fee().doubleValue()) / 100.0), 2,
        RoundingMode.UP);

    assertEquals(txId, createdAction.transactionId());
    assertEquals(expectedRevenue, offerStat.notConfirmedRevenue());
    assertEquals(offerId, (long) offerStat.master());
  }

  private InputSupplier<InputStream> input(final String str) {
    return new InputSupplier<InputStream>() {

      @Override
      public InputStream getInput() throws IOException {
        return new ByteArrayInputStream(str.getBytes());
      }
    };
  }
}
