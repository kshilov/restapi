package com.heymoose.domain.action;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class ItemWithStatus extends Item {
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

  @Override
  public String toString() {
    return Objects.toStringHelper(ItemWithStatus.class)
        .add("id", this.id())
        .add("price", this.price())
        .add("status", this.status())
        .toString();
  }
}
