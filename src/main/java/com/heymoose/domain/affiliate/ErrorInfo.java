package com.heymoose.domain.affiliate;

import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;


@Entity
@Table(name = "error_info")
public final class ErrorInfo {


  @Embeddable
  private static class ErrorKey implements Serializable {
    @Column(name = "affiliate_id")
    private Long affiliateId;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Basic(optional = false)
    private String description;

  }

  public static ErrorInfo fromException(Long affiliateId,
                                        String uri,
                                        DateTime date,
                                        Throwable cause) {

    String description = String.format(
        "%s %s", cause.getClass().getName(), cause.getMessage());

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    cause.printStackTrace(printWriter);
    String stackTrace = stringWriter.toString();

    return new ErrorInfo()
        .setAffiliateId(affiliateId)
        .setLastOccurred(DateTime.now())
        .setUri(uri)
        .setDescription(description)
        .setStackTrace(stackTrace)
        .setLastOccurred(date);
  }

  @Id
  private ErrorKey key;

  @Column(name = "last_occurred", nullable = false)
  private Date lastOccurred;

  @Column(name = "stack_trace", length = 10000)
  private String stackTrace;


  public ErrorInfo() {
    this.key = new ErrorKey();
  }

  public Long affiliateId() {
    return key.affiliateId;
  }

  public String description() {
    return key.description;
  }

  public DateTime date() {
    return new DateTime(lastOccurred);
  }

  public String stackTrace() {
    return stackTrace;
  }

  public String uri() {
    return key.uri;
  }

  public ErrorInfo setAffiliateId(Long affiliateId) {
    this.key.affiliateId = affiliateId;
    return this;
  }

  public ErrorInfo setUri(String uri) {
    this.key.uri = uri;
    return this;
  }

  public ErrorInfo setDescription(String description) {
    this.key.description = description;
    return this;
  }

  public ErrorInfo setLastOccurred(DateTime lastOccurred) {
    this.lastOccurred = lastOccurred.toDate();
    return this;
  }

  public ErrorInfo setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
    return this;
  }


}
