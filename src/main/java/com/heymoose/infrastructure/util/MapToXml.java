package com.heymoose.infrastructure.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jdom2.Element;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Map;

import static com.heymoose.resource.xml.JDomUtil.element;

public final class MapToXml {

  private final ImmutableSet.Builder<String> attributeNameSet =
      ImmutableSet.builder();
  private String elementName;
  private String prefix = "";
  private ImmutableMap.Builder<String, String> aliasMap =
      ImmutableMap.builder();

  public Element execute(Map<String, ?> map) {
    Preconditions.checkNotNull(elementName, "Name an element!");
    ImmutableSet<String> attributes = attributeNameSet.build();
    Element element = new Element(elementName);
    Map<String, String> aliases = aliasMap.build();
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (!entry.getKey().startsWith(prefix)) continue;

      String key = entry.getKey().substring(prefix.length());

      if (aliases.containsKey(key)) key = aliases.get(key);

      String value = value(entry.getValue());
      if (attributes.contains(key)) {
        element.setAttribute(key, value);
      } else {
        element.addContent(element(key, value));
      }
    }
    return element;
  }

  private String value(Object value) {
    if (value instanceof Timestamp) {
      return new DateTime(((Timestamp) value).getTime()).toString();
    } else {
      return value.toString();
    }
  }

  public MapToXml setElementName(String name) {
    this.elementName = name;
    return this;
  }

  public MapToXml addAttributeName(String name) {
    this.attributeNameSet.add(name);
    return this;
  }

 public MapToXml setPrefix(String prefix) {
   this.prefix = prefix;
   return this;
  }

  public MapToXml addAlias(String keyName, String childName) {
    this.aliasMap.put(keyName, childName);
    return this;
  }
}
