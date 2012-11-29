package com.heymoose.resource.xml;


import org.jdom2.Document;
import org.jdom2.Element;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces("application/xml")
@Singleton
public final class JDomElementWriter implements MessageBodyWriter<Element> {
  @Override
  public boolean isWriteable(Class<?> type,  Type genericType,
                             Annotation[] annotations, MediaType mediaType) {
    return type.isAssignableFrom(Element.class);
  }

  @Override
  public long getSize(Element element,
                      Class<?> type, Type genericType,
                      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Element element, Class<?> type, Type genericType,
                      Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream)
      throws IOException, WebApplicationException {
    JDomUtil.XML_OUTPUTTER.output(new Document(element), entityStream);
  }
}
