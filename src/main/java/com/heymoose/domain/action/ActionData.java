package com.heymoose.domain.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used for importing actions.
 */
public final class ActionData {

  public enum Status {
    CREATED, COMPLETE, CANCELED
  }

  public static class Item {
    private long id;
    private BigDecimal price;
    private int quantity = 1;

    public Item(long id) {
      this.id = id;
    }

    public Item(long id, BigDecimal price) {
      this.id = id;
      this.price = price;
    }

    public Item(long id, BigDecimal price, int quantity) {
      this.id = id;
      this.price = price;
      this.quantity = quantity;
    }

    public long id() {
      return id;
    }

    public BigDecimal price() {
      return price;
    }

    public int quantity() {
      return quantity;
    }
  }

  private String token;
  private String transactionId;
  private Status status;
  private List<Item> itemList = Lists.newArrayList();

  public ActionData() { }

  public String token() {
    return token;
  }

  public String transactionId() {
    return transactionId;
  }

  public Status status() {
    return status;
  }

  public List<Item> itemList() {
    return ImmutableList.copyOf(itemList);
  }

  public ActionData setToken(String token) {
    this.token = token;
    return this;
  }

  public ActionData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ActionData setStatus(Status status) {
    this.status = status;
    return this;
  }

  public ActionData addItem(long itemId) {
    this.itemList.add(new Item(itemId));
    return this;
  }
}
