package com.heymoose.infrastructure.util;

import com.google.common.collect.ImmutableMap;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;

public final class MapToXmlTest {

  private static final XMLOutputter OUT = new XMLOutputter();
  private static final Logger log =
      LoggerFactory.getLogger(MapToXmlTest.class);

  @Test
  public void noPrefixNoAttributesTest() throws Exception {
    MapToXml mapper = new MapToXml()
        .setElementName("element-name")
        .addChild("key");
    Element element = mapper.execute(ImmutableMap.of("key", "value"));


    log.info("{}", OUT.outputString(element));
    assertEquals(1, element.getContentSize());
    assertEquals("element-name", element.getName());
    assertEquals("key", element.getChildren().get(0).getName());
    assertEquals("value", element.getChildren().get(0).getValue());
  }

  @Test
  public void attribute() throws Exception {
    MapToXml mapper = new MapToXml()
        .setElementName("name")
        .addAttributeName("id");
    Element element = mapper.execute(ImmutableMap.of("id", "value"));

    assertEquals("id", element.getAttributes().get(0).getName());
    assertEquals("value", element.getAttributeValue("id"));
  }

  @Test
  public void prefix() throws Exception {
    MapToXml mapper = new MapToXml()
        .setElementName("offer")
        .setPrefix("offer_")
        .addChild("id");
    Element element = mapper.execute(ImmutableMap.of("offer_id", "value"));

    log.info("{}", OUT.outputString(element));
    assertEquals("value", element.getChild("id").getText());
  }

  @Test
  public void alias() throws Exception {
    MapToXml mapper = new MapToXml()
        .setElementName("element")
        .addChild("name", "full-name");
    Element element = mapper.execute(ImmutableMap.of("name", "Moose"));

    log.info("{}", OUT.outputString(element));
    assertEquals("Moose", element.getChildText("full-name"));
  }
}
