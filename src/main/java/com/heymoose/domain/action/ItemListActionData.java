package com.heymoose.domain.action;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used for importing actions.
 */
public final class ItemListActionData extends ActionData {

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

    public String toString() {
      return Objects.toStringHelper(Item.class)
          .add("id", id)
          .add("price", price)
          .add("quantity", quantity)
          .toString();
    }
  }
  private List<Item> itemList = Lists.newArrayList();
  private ActionStatus status;

  public ItemListActionData() { }

  public List<Item> itemList() {
    return ImmutableList.copyOf(itemList);
  }

  public ItemListActionData addItem(String itemId) {
    this.itemList.add(new Item(itemId));
    return this;
  }

  public ItemListActionData addItem(String itemId, BigDecimal price, int quantity) {
    this.itemList.add(new Item(itemId, price, quantity));
    return this;
  }

  public ActionStatus status() {
    return status;
  }

  public ItemListActionData setStatus(ActionStatus status) {
    this.status = status;
    return this;
  }
}
