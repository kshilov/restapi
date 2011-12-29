package com.heymoose.domain;

public class Context {
  public final App app;
  public final Integer hour;

  public Context(App app, Integer hour) {
    this.app = app;
    this.hour = hour;
  }
}
