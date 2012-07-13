package com.heymoose.resource.xml;

import com.google.common.base.Objects;
import com.heymoose.domain.errorinfo.ErrorInfo;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public final class XmlErrorInfo {

  private static DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  @XmlAttribute
  public Long id;
  @XmlElement
  public String description;
  @XmlElement
  public String uri;
  @XmlElement(name = "last-occurred")
  public String lastOccurred;
  @XmlElement(name = "occurrence-count")
  public Long occurrenceCount;
  @XmlElement(name = "stack-trace")
  public String stackTrace;

  public XmlErrorInfo() { }

  public XmlErrorInfo(ErrorInfo info) {
    this.id = info.id();
    this.description = info.description();
    this.uri = info.uri();
    this.lastOccurred = info.lastOccurred().toString(ISO);
    this.occurrenceCount = info.occurrenceCount();
    this.stackTrace = info.stackTrace();
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(
        description,
        uri,
        lastOccurred,
        occurrenceCount,
        stackTrace);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof XmlErrorInfo) {
      XmlErrorInfo that = (XmlErrorInfo) o;
      return Objects.equal(this.description, that.description)
          && Objects.equal(this.uri, that.uri)
          && Objects.equal(this.lastOccurred, that.lastOccurred)
          && Objects.equal(this.occurrenceCount, that.occurrenceCount)
          && Objects.equal(this.stackTrace, that.stackTrace);
    }
    return false;
  }
}
