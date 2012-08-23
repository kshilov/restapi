package com.heymoose.resource.xml;

import com.heymoose.infrastructure.util.QueryResult;

import java.util.Map;

public final class XmlQueryResult {

  private final QueryResult queryResult;
  private String root;
  private String element;
  private StringBuilder builder = new StringBuilder();

  public XmlQueryResult(QueryResult result) {
    this.queryResult = result;
  }

  public XmlQueryResult(String root, String entry, QueryResult queryResult) {
    this.root = root;
    this.element = entry;
    this.queryResult = queryResult;
  }

  public XmlQueryResult setRoot(String root) {
    this.root = root;
    return this;
  }

  public XmlQueryResult setElement(String elementName) {
    this.element = elementName;
    return this;
  }

  @Override
  public String toString() {
    open(root);
    for (Map<String, Object> record : queryResult) {
      open(element);
          for (Map.Entry<String, Object> entry : record.entrySet()) {
            open(entry.getKey());
            builder.append(entry.getValue());
            close(entry.getKey());
          }
      close(element);
    }
    close(root);
    return builder.toString();
  }

  private void open(String tag) {
    builder.append("<");
    builder.append(tag);
    builder.append(">");
  }

  private void close(String tag) {
    builder.append("</");
    builder.append(tag);
    builder.append(">");
  }

}
