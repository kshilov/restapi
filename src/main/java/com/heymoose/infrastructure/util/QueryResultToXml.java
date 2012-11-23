package com.heymoose.infrastructure.util;

import com.google.common.collect.ImmutableList;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.jdom2.Element;

import java.util.List;
import java.util.Map;

public final class QueryResultToXml {

  private String elementName;
  private ImmutableList.Builder<MapToXml> mapperList = ImmutableList.builder();

  public Element execute(QueryResult result) {
    Element root = new Element(elementName);
    List<MapToXml> mappers = mapperList.build();
    for (Map<String, Object> map : result) {
      for (MapToXml mapper : mappers) {
        root.addContent(mapper.execute(map));
      }
    }
    return root;
  }

  public QueryResultToXml setElementName(String name) {
    this.elementName = name;
    return this;
  }

  public QueryResultToXml addMapToXml(MapToXml mapper) {
    this.mapperList.add(mapper);
    return this;
  }
}
