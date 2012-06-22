package com.heymoose.resource.xml;

import com.google.common.base.Objects;
import com.heymoose.domain.affiliate.ErrorInfo;

/**
 * @author Andrey Salomatin
 */
public final class XmlErrorInfo {

  public Long affiliateId;

  public String message;

  public XmlErrorInfo() { }

  public XmlErrorInfo(ErrorInfo info) {
    this.affiliateId = info.affiliateId();
    this.message = info.description();
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(affiliateId, message);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof XmlErrorInfo) {
      XmlErrorInfo that = (XmlErrorInfo) o;
      return Objects.equal(this.affiliateId, that.affiliateId)
          && Objects.equal(this.message, that.message);
    }
    return false;
  }
}
