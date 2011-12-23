package com.heymoose.resource.api;

public class ApiRequestException extends Exception {
  public final int status;

  public ApiRequestException(int status, String message) {
    super(message);
    this.status = status;
  }
}
