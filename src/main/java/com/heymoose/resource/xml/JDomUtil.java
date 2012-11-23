package com.heymoose.resource.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public final class JDomUtil {

  public static final XMLOutputter XML_OUTPUTTER = new XMLOutputter();


  private JDomUtil() { }

  public static Element element(String name, Object innerText) {
    Element element = new Element(name);
    if (innerText != null) element.setText(innerText.toString());
    return element;
  }

  public static String toXmlString(Element root) {
    return XML_OUTPUTTER.outputString(new Document(root));
  }
}
