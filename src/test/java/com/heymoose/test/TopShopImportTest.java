package com.heymoose.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.service.topshop.TopShopDataImporter;
import com.heymoose.infrastructure.service.topshop.TopShopXmlConverter;
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
import java.util.Map;

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
        OFFER_BALANCE, advId, "http://something.com",false);
    heymoose().doCreateGrant(offerId, affId);
    heymoose().doClick(offerId, affId, null, Subs.empty(), null);
    Token token = select(Token.class).get(0);
    log.info("Token id: {}", token.id());

    String itemCode = "topshop-item-code";
    String txId = "top-shop-order-id";
    Double price = 100.0;
    TopShopDataImporter importer = topshop(itemCode, offerId);
    TopShopXmlConverter converter = new TopShopXmlConverter();
    String topShopXml =
        "<payment_list>" +
          "<payment>" +
            "<order_id>" + txId + "</order_id>" +
            "<key>http://anyurl.com?_hm_token=" + token.value() + "</key>" +
            "<item_list>" +
              "<item>" +
                "<code>" + itemCode + "</code>" +
                "<price>" + price.toString() + "</price>" +
              "</item>" +
            "</item_list>" +
          "</payment>" +
        "</payment_list>";
    // manually start transaction, because not using guice
    Session session = injector().getProvider(Session.class).get();
    Transaction tx = session.beginTransaction();
    try {
      importer.doImport(converter.convert(input(topShopXml)));
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
    BigDecimal expectedRevenue = cost.divide(new BigDecimal((100 + affiliate.fee()) / 100.0), 2, RoundingMode.CEILING);

    assertEquals(txId, createdAction.transactionId());
    assertEquals(expectedRevenue, offerStat.notConfirmedRevenue());
  }

  private TopShopDataImporter topshop(String itemCode, Long offerId) {
    Map<String, Long> topShopOfferMap = ImmutableMap.of(itemCode, offerId);
    return new TopShopDataImporter(
        injector().getInstance(Repo.class),
        injector().getInstance(Tracking.class),
        topShopOfferMap);
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
