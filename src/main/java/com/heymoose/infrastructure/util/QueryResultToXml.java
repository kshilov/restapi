package com.heymoose.infrastructure.util;

import com.heymoose.infrastructure.util.db.QueryResult;
import org.jdom2.Element;

import java.util.Map;

public final class QueryResultToXml {

  private String elementName;
  private MapToXml mapper;

  public Element execute(QueryResult result) {
    Element root = new Element(elementName);
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
}
