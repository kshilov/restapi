package com.heymoose.test;

import com.google.common.base.Strings;
import com.heymoose.domain.affiliate.ErrorInfo;
import com.heymoose.resource.xml.XmlErrorInfo;
import com.heymoose.resource.xml.XmlErrorsInfo;
import com.heymoose.test.base.RestTest;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;

public final class ErrorLoggingTest extends RestTest {

  @After
  public void tearDown() {
    reset();
  }

  @Test
  public void savesErrorInDb() throws Exception {
    DateTime testStartTime = DateTime.now();
    heymoose().errorClick(1L);
    List<ErrorInfo> savedErrorList = select(ErrorInfo.class);

    assertEquals(1, savedErrorList.size());

    ErrorInfo savedError = savedErrorList.get(0);
    log.info("Error saved: {}", savedError);
    assertTrue(savedError.lastOccurred().isAfter(testStartTime));
    assertFalse(Strings.isNullOrEmpty(savedError.description()));
    assertFalse(Strings.isNullOrEmpty(savedError.stackTrace()));
    assertEquals(1, (long) savedError.occurrenceCount());
  }

  @Test
  public void doesNotStoreDuplicatesOnlyUpdatesOccurrence()
      throws Exception {
    Long affiliateId = 1L;

    heymoose().errorClick(affiliateId);
    DateTime firstErrorTime = select(ErrorInfo.class).get(0).lastOccurred();
    heymoose().errorClick(affiliateId);

    List<ErrorInfo> savedErrorList = select(ErrorInfo.class);
    assertEquals(1, savedErrorList.size());
    ErrorInfo secondError = savedErrorList.get(0);
    DateTime secondErrorTime = secondError.lastOccurred();
    assertTrue(secondErrorTime.isAfter(firstErrorTime));
    assertEquals(2, (long) secondError.occurrenceCount());
    log.info("First error time: [{}]. Second error time: [{}]",
        firstErrorTime, secondErrorTime);
  }

  @Test
  public void returnsEmptyXmlIfNoErrorsOccurred() throws Exception {
    XmlErrorsInfo xml = heymoose().listApiErrors(1L);

    assertEquals(0, (long) xml.count);
    assertEquals(0, xml.list.size());
  }

  @Test
  public void returnsOccurredErrorCorrectly() throws Exception {
    Long affiliateId = 1L;

    heymoose().errorClick(affiliateId);
    XmlErrorsInfo xmlErrorList = heymoose().listApiErrors(affiliateId);
    XmlErrorInfo xmlError = xmlErrorList.list.get(0);

    ErrorInfo error = select(ErrorInfo.class).get(0);
    assertEquals(error.lastOccurred(),
        ISODateTimeFormat.dateTime().parseDateTime(xmlError.lastOccurred));
    assertEquals(error.description(), xmlError.description);
    assertEquals(error.uri(), xmlError.uri);
    assertEquals(error.occurrenceCount(), xmlError.occurrenceCount);
  }
}
