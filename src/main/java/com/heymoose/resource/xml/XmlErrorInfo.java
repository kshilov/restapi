package com.heymoose.resource.xml;

import com.google.common.base.Objects;
import com.heymoose.domain.affiliate.ErrorInfo;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "error")
public final class XmlErrorInfo {

  private static DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  @XmlValue
  public Long affiliateId;
  @XmlValue
  public String description;
  @XmlValue
  public String uri;
  @XmlValue
  public String lastOccurred;
  @XmlValue
  public String stackTrace;

  public XmlErrorInfo() { }

  public XmlErrorInfo(ErrorInfo info) {
    this.affiliateId = info.affiliateId();
    this.description = info.description();
    this.uri = info.uri();
    this.lastOccurred = info.lastOccurred().toString(ISO);
    this.stackTrace = info.stackTrace();
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(
        affiliateId,
        description,
        uri,
        lastOccurred,
        stackTrace);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof XmlErrorInfo) {
      XmlErrorInfo that = (XmlErrorInfo) o;
      return Objects.equal(this.affiliateId, that.affiliateId)
          && Objects.equal(this.description, that.description)
          && Objects.equal(this.uri, that.uri)
          && Objects.equal(this.lastOccurred, that.lastOccurred)
          && Objects.equal(this.stackTrace, that.stackTrace);
    }
    return false;
  }
}
