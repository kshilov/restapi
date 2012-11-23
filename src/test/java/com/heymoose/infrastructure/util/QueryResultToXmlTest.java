package com.heymoose.infrastructure.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class QueryResultToXmlTest {

  private static final XMLOutputter OUT = new XMLOutputter();
  private static final Logger log =
      LoggerFactory.getLogger(QueryResultToXmlTest.class);

  @Test
  public void oneRecord() throws Exception {
    QueryResult result = oneEntryResult("key", "value");
    QueryResultToXml transformer = new QueryResultToXml()
        .setElementName("results")
        .addMapToXml(new MapToXml()
            .setElementName("result"));
    Element resultsXml = transformer.execute(result);

    log.info("{}", OUT.outputString(resultsXml));
    assertEquals(1, resultsXml.getChildren().size());
    assertEquals("value", resultsXml.getChild("result").getChildText("key"));
  }

  @Test
  public void twoPrefixes() throws Exception {
    QueryResult result = queryResultWith(ImmutableMap.of(
        "id", "id",
        "offer_id", "offer_id"));
    QueryResultToXml transformer = new QueryResultToXml()
        .setElementName("results")
        .addMapToXml(new MapToXml()
            .setElementName("result"))
        .addMapToXml(new MapToXml()
            .setElementName("offer")
            .setPrefix("offer_"));
    Element xmlResult = transformer.execute(result);

    log.info("{}", OUT.outputString(xmlResult));
  }

  @SuppressWarnings("unchecked")
  private QueryResult queryResultWith(Map<String, ? extends Object> entry) {
    List<Map<String, Object>> list =
        ImmutableList.of((Map<String, Object>) entry);
    return new QueryResult(list);

  }

  private QueryResult oneEntryResult(String key, String value) {
    Map<String, Object> map = ImmutableMap.<String, Object>of(key, value);
    List<Map<String, Object>> list = ImmutableList.of(map);
    return new QueryResult(list);
  }
}
