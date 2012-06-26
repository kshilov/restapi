package com.heymoose.domain.affiliate;

import com.google.common.base.Objects;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;


@Entity
@Table(name = "error_info")
public final class ErrorInfo {

  public static ErrorInfo fromException(String uri,
                                        DateTime date,
                                        Throwable cause) {

    String description = String.format(
        "%s %s", cause.getClass().getName(), cause.getMessage());

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    cause.printStackTrace(printWriter);
    String stackTrace = stringWriter.toString();

    return new ErrorInfo()
        .setLastOccurred(DateTime.now())
        .setUri(uri)
        .setDescription(description)
        .setStackTrace(stackTrace)
        .setLastOccurred(date);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error-info-seq")
  @SequenceGenerator(name = "error-info-seq", sequenceName = "error_info_seq", allocationSize = 1)
  private Long id;

  @Column(name = "uri", nullable = false)
  private String uri;

  @Basic(optional = false)
  private String description;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "last_occurred", nullable = false)
  private DateTime lastOccurred;

  @Column(name = "occurrence_count", nullable = false)
  private Long occurrenceCount = 1L;

  @Column(name = "stack_trace", length = 10000)
  private String stackTrace;


  public ErrorInfo() {
  }

  public String description() {
    return description;
  }

  public DateTime lastOccurred() {
    return new DateTime(lastOccurred);
  }

  public String stackTrace() {
    return stackTrace;
  }

  public String uri() {
    return uri;
  }

  public Long occurrenceCount() {
    return occurrenceCount;
  }

  public Long id() {
    return id;
  }

  public ErrorInfo setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public ErrorInfo setDescription(String description) {
    this.description = description;
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
        .add("uri", uri())
        .add("description", description()).toString();
  }
}
