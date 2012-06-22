package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "error_info")
public final class ErrorInfo extends IdEntity {


  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error-info-seq")
  @SequenceGenerator(name = "error-info-seq", sequenceName = "error_info_seq", allocationSize = 1)
  private Long id;

  @Column(name = "affiliate_id")
  private Long affiliateId;

  @Column(name = "uri", nullable = false)
  private String uri;

  @Basic(optional = false)
  private String description;

  @Column(name = "last_occurred", nullable = false)
  private DateTime lastOccurred;

  @Column(name = "stack_trace", length = 10000)
  private String stackTrace;

  public ErrorInfo() { }

  public ErrorInfo(Long affiliateId, Exception exception) {
    this.affiliateId = affiliateId;
    this.description = exception.getMessage();
  }

  @Override
  public Long id() {
    return null;
  }

  public Long affiliateId() {
    return affiliateId;
  }

  public String description() {
    return description;
  }

  public DateTime date() {
    return lastOccurred;
  }

  public String stackTrace() {
    return stackTrace;
  }

  public String uri() {
    return uri;
  }

  public ErrorInfo setAffiliateId(Long affiliateId) {
    this.affiliateId = affiliateId;
    return this;
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


}
