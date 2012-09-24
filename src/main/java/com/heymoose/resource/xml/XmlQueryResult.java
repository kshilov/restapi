package com.heymoose.resource.xml;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.QueryResult;

import java.sql.Timestamp;
import java.util.Map;

import org.joda.time.DateTime;

public final class XmlQueryResult {

  private static final String HEAD =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  private final QueryResult queryResult;
  private String root;
  private String element;
  private StringBuilder builder = new StringBuilder();
  private ImmutableMap.Builder<String, Object> rootAttributeMap =
      new ImmutableMap.Builder<String, Object>();

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

  public XmlQueryResult addRootAttribute(String name, Object value) {
    this.rootAttributeMap.put(name, value);
    return this;
  }

  public XmlQueryResult addRootAttributesFrom(Map<String, ?> map) {
    this.rootAttributeMap.putAll(map);
    return this;
  }

  @Override
  public String toString() {
    builder.append(HEAD);

    if (!Strings.isNullOrEmpty(root))
      open(root, rootAttributeMap.build());

    for (Map<String, Object> record : queryResult) {
      open(element);
          for (Map.Entry<String, Object> entry : record.entrySet()) {
            open(entry.getKey());
            Object val = entry.getValue();
            if (val instanceof Timestamp)
              builder.append(new DateTime(((Timestamp) val).getTime()));
            else
              builder.append(entry.getValue());
            close(entry.getKey());
          }
      close(element);
    }

    if (!Strings.isNullOrEmpty(root))
      close(root);

    return builder.toString();
  }

  private void open(String tag) {
    builder.append("<");
    builder.append(tag);
    builder.append(">");
  }

  private void open(String tag, Map<String, Object> attributes) {
    builder.append("<");
    builder.append(tag);
    for (Map.Entry<String, Object> attr : attributes.entrySet()) {
      builder.append(' ');
      builder.append(attr.getKey());
      builder.append("=\"");
      builder.append(attr.getValue());
      builder.append("\"");
    }
    builder.append(">");
  }

  private void close(String tag) {
    builder.append("</");
    builder.append(tag);
    builder.append(">");
  }

}
