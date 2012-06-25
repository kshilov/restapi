package com.heymoose.domain.affiliate;

import com.google.common.base.Objects;
import org.hibernate.annotations.Type;
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

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "last_occurred", nullable = false)
  private DateTime lastOccurred;

  @Column(name = "occurrence_count", nullable = false)
  private Long occurrenceCount = 1L;

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

  public DateTime lastOccurred() {
    return new DateTime(lastOccurred);
  }

  public String stackTrace() {
    return stackTrace;
  }

  public String uri() {
    return key.uri;
  }

  public Long occurrenceCount() {
    return occurrenceCount;
  }

  public ErrorKey id() {
    return key;
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
    this.lastOccurred = lastOccurred;
    return this;
  }

  public ErrorInfo setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
    return this;
  }

  public ErrorInfo setOccurrenceCount(Long count) {
    this.occurrenceCount = count;
    return this;
  }

  public ErrorInfo fillFromErrorInfo(ErrorInfo that) {
    this.setAffiliateId(that.affiliateId());
    this.setUri(that.uri());
    this.setDescription(that.description());
    this.setLastOccurred(that.lastOccurred());
    this.setOccurrenceCount(that.occurrenceCount());
    this.setStackTrace(that.stackTrace());
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(ErrorInfo.class)
        .add("affiliateId", affiliateId())
        .add("uri", uri())
        .add("description", description()).toString();
  }
}
