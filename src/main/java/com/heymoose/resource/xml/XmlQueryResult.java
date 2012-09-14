package com.heymoose.resource.xml;

import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;

import java.util.Map;

public final class XmlQueryResult {

  private static final String HEAD =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  private final QueryResult queryResult;
  private String root;
  private String element;
  private Long count;
  private StringBuilder builder = new StringBuilder();

  public XmlQueryResult(QueryResult result) {
    this.queryResult = result;
  }

  public XmlQueryResult(Pair<QueryResult, Long> result) {
    this.queryResult = result.fst;
    this.count = result.snd;
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

  public XmlQueryResult setCount(Long count) {
    this.count = count;
    return this;
  }

  @Override
  public String toString() {
    builder.append(HEAD);
    if (count == null) {
      open(root);
    } else {
      open(root, "count=\"" + count + "\"");
    }
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

  private void open(String tag, String... attrString) {
    builder.append("<");
    builder.append(tag);
    for (String keyVal : attrString) {
      builder.append(' ');
      builder.append(keyVal);
    }
    builder.append(">");
  }

  private void close(String tag) {
    builder.append("</");
    builder.append(tag);
    builder.append(">");
  }

}
