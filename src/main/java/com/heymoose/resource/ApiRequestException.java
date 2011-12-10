package com.heymoose.resource;

public class ApiRequestException extends Exception {
  public final int status;

  public ApiRequestException(int status, String message) {
    super(message);
    this.status = status;
  }
}
