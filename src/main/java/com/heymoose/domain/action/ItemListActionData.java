package com.heymoose.domain.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used for importing actions.
 */
public final class ItemListActionData extends ActionData {

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
