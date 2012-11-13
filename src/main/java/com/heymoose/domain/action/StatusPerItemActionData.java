package com.heymoose.domain.action;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;

public final class StatusPerItemActionData extends ActionData {
  public static class ItemWithStatus extends Item {
    private ActionStatus status;

    public ItemWithStatus(String id) {
      super(id);
    }

    public ItemWithStatus(String id, BigDecimal price) {
      super(id, price);
    }

    public ItemWithStatus(String id, BigDecimal price, int quantity) {
      super(id, price, quantity);
    }

    public ActionStatus status() {
      return this.status;
    }

    public ItemWithStatus setStatus(ActionStatus status) {
      this.status = status;
      return this;
    }

    public ItemWithStatus setStatus(String status) {
      if (status == null) {
        this.status = ActionStatus.CREATED;
        return this;
      }
      int statusNum = Integer.valueOf(status);
      this.status = ActionStatus.values()[statusNum];
      return this;
    }
  }
  private ImmutableList.Builder<ItemWithStatus> itemList =
      ImmutableList.builder();

  public StatusPerItemActionData addItem(ItemWithStatus item) {
    this.itemList.add(item);
    return this;
  }

  public ImmutableList<ItemWithStatus> itemList() {
    return this.itemList.build();
  }
}
