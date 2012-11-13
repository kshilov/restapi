package com.heymoose.test;

import com.google.common.io.InputSupplier;
import com.heymoose.test.base.RestTest;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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


  private InputSupplier<InputStream> input(final String str) {
    return new InputSupplier<InputStream>() {

      @Override
      public InputStream getInput() throws IOException {
        return new ByteArrayInputStream(str.getBytes());
      }
    };
  }
}
