package com.heymoose.domain.action;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class Item {
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

  public Item setPrice(String price) {
    if (price == null) {
      this.price = null;
      return this;
    }
    this.price = new BigDecimal(price);
    return this;
  }

  public Item setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  public Item setQuantity(String quantity) {
    if (quantity == null) {
      this.quantity = 1;
      return this;
    }
    this.quantity = Integer.valueOf(quantity);
    return this;
  }

  public Item setId(String id) {
    this.id = id;
    return this;
  }


  public String toString() {
    return Objects.toStringHelper(Item.class)
        .add("id", id)
        .add("price", price)
        .add("quantity", quantity)
        .toString();
  }
}
