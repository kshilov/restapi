package com.heymoose.domain.affiliate;

public final class ErrorInfo {

  private Long affiliateId;

  private String message;

  protected ErrorInfo() { }

  public ErrorInfo(Long affiliateId, Exception exception) {
    this.affiliateId = affiliateId;
    this.message = exception.getMessage();
  }

  public Long affiliateId() {
    return affiliateId;
  }

  public String message() {
    return message;
  }

}
