package com.heymoose.infrastructure.util;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.jdom2.Element;

import java.util.Map;

public final class QueryResultToXml {

  private String elementName;
  private MapToXml mapper;
  private ImmutableMap.Builder<String, String> attributeMap =
      ImmutableMap.builder();

  public Element execute(QueryResult result) {
    Element root = new Element(elementName);
    for (Map.Entry<String, String> attr : attributeMap.build().entrySet()) {
      root.setAttribute(attr.getKey(), attr.getValue());
    }
    for (Map<String, Object> map : result) {
      root.addContent(mapper.execute(map));
    }
    return root;
  }

  public QueryResultToXml setElementName(String name) {
    this.elementName = name;
    return this;
  }

  public QueryResultToXml setMapper(MapToXml mapper) {
    this.mapper = mapper;
    return this;
  }

  public QueryResultToXml setAttribute(String name, String value) {
    this.attributeMap.put(name, value);
    return this;
  }
}
