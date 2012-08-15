package com.heymoose.domain.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used for importing actions.
 */
public final class ItemListActionData {

  public static class Item {
    private String id;
    private BigDecimal price;
    private int quantity = 1;

    public Item(String id) {
      this.id = id;
    }

    public Item(String id, BigDecimal price) {
      this.id = id;
      this.price = price;
    }

    public Item(String id, BigDecimal price, int quantity) {
      this.id = id;
      this.price = price;
      this.quantity = quantity;
    }

    public String id() {
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
  private ActionStatus status;
  private List<Item> itemList = Lists.newArrayList();

  public ItemListActionData() { }

  public String token() {
    return token;
  }

  public String transactionId() {
    return transactionId;
  }

  public ActionStatus status() {
    return status;
  }

  public List<Item> itemList() {
    return ImmutableList.copyOf(itemList);
  }

  public ItemListActionData setToken(String token) {
    this.token = token;
    return this;
  }

  public ItemListActionData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ItemListActionData setStatus(ActionStatus status) {
    this.status = status;
    return this;
  }

  public ItemListActionData addItem(String itemId) {
    this.itemList.add(new Item(itemId));
    return this;
  }

  public ItemListActionData addItem(String itemId, BigDecimal price, int quantity) {
    this.itemList.add(new Item(itemId, price, quantity));
    return this;
  }
}
