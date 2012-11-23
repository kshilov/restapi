package com.heymoose.infrastructure.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
  private ImmutableMap.Builder<String, String> childNameMap =
      ImmutableMap.builder();
  private ImmutableList.Builder<MapToXml> subMapperList =
      ImmutableList.builder();

  public Element execute(Map<String, ?> map) {
    Preconditions.checkNotNull(elementName, "Name an element!");
    ImmutableSet<String> attributes = attributeNameSet.build();
    Element element = new Element(elementName);
    Map<String, String> children = childNameMap.build();

    for (MapToXml subMapper : subMapperList.build()) {
      element.addContent(subMapper.execute(map));
    }

    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (!entry.getKey().startsWith(prefix)) continue;

      String key = entry.getKey().substring(prefix.length());

      if (!children.containsKey(key) && !attributes.contains(key)) continue;

      String value = value(entry.getValue());
      if (attributes.contains(key)) {
        element.setAttribute(key, value);
        continue;
      }
      element.addContent(element(children.get(key), value));
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

  public MapToXml addAttribute(String name) {
    this.attributeNameSet.add(name);
    return this;
  }

 public MapToXml setPrefix(String prefix) {
   this.prefix = prefix;
   return this;
  }

  public MapToXml addChild(String keyName, String alias) {
    this.childNameMap.put(keyName, alias);
    return this;
  }

  public MapToXml addChild(String key) {
    this.childNameMap.put(key, key);
    return this;
  }

  public MapToXml addSubMapper(MapToXml subMapper) {
    if (Strings.isNullOrEmpty(subMapper.prefix)) {
      subMapper.prefix = subMapper.elementName + "_";
    }
    this.subMapperList.add(subMapper);
    return this;
  }
}
