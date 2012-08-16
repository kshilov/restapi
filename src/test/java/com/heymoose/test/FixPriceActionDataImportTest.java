package com.heymoose.test;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.action.FixPriceActionDataImporter;
import com.heymoose.infrastructure.service.action.HeymooseFixPriceParser;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class FixPriceActionDataImportTest extends RestTest {

  private static final Logger log =
      LoggerFactory.getLogger(FixPriceActionDataImportTest.class);

  private final static double OFFER_BALANCE = 70.0;
  private final static double OFFER_COST = 20.0;
  private final static String OFFER_CODE = "offer-code";

  @Before
  public void before() {
    reset();
  }

  @Test
  public void singlePaymentImport() throws Exception {
    long advId = heymoose().doRegisterAdvertiser(100.0);
    long affId = heymoose().doRegisterAffiliate();
    long offerId = heymoose().doCreateCpaOffer(
        CpaPolicy.FIXED, OFFER_COST, null,
        OFFER_BALANCE, advId, OFFER_CODE);
    heymoose().doCreateGrant(offerId, affId);
    heymoose().doClick(offerId, affId, null, Subs.empty(), null);
    Token token = select(Token.class).get(0);
    log.info("Token id: {}", token.id());

    String xml =
        "<actions>" +
          "<action>" +
          "<transaction>transaction</transaction>" +
          "<token>" + token.value() + "</token>" +
          "<offer>" + OFFER_CODE + "</offer>" +
          "<status>0</status>" +
          "</action>" +
        "</actions>";

    FixPriceActionDataImporter importer =
        injector().getInstance(FixPriceActionDataImporter.class);
    HeymooseFixPriceParser parser = new HeymooseFixPriceParser();
    List<FixPriceActionData> actionData = parser
        .parse(input(xml));
    importer.doImport(actionData, offerId);

    XmlUser aff = heymoose().getUser(affId);
    XmlOffer offer = heymoose().getOffer(offerId);
    assertEquals(
        offer.affiliateCost.doubleValue(),
        aff.affiliateAccountNotConfirmed.balance, 0.0001);
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
