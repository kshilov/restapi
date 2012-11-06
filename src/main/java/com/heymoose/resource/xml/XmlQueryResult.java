package com.heymoose.resource.xml;

import com.google.common.collect.ImmutableMap;
import com.heymoose.infrastructure.util.QueryResult;
import org.jdom2.Namespace;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Map;

public final class XmlQueryResult {

  private final QueryResult queryResult;
  private String root;
  private String element;
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
    Element rootElement = new Element(root);
    for (Map.Entry<String, Object> entry : rootAttributeMap.build().entrySet()) {
      if (entry.getKey().equals("xmlns")) {
        String namespaceString = entry.getValue().toString();
        Namespace namespace = Namespace.getNamespace(namespaceString);
        rootElement.setNamespace(namespace);
        continue;
      }
      rootElement.setAttribute(entry.getKey(), entry.getValue().toString());
    }

    for (Map<String, Object> record : queryResult) {
      Element childElement = new Element(element);
        for (Map.Entry<String, Object> entry : record.entrySet()) {
          Element attributeElement = new Element(entry.getKey());
          Object val = entry.getValue();
          if (val instanceof Timestamp) {
            attributeElement.setText(
                new DateTime(((Timestamp) val).getTime()).toString());
          } else {
            attributeElement.setText(val.toString());
          }
          childElement.addContent(attributeElement);
        }
      rootElement.addContent(childElement);
    }
    return new XMLOutputter().outputString(new Document(rootElement));
  }

}
