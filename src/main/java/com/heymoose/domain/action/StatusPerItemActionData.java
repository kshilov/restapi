package com.heymoose.domain.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public final class StatusPerItemActionData extends ActionData {

  private ImmutableList.Builder<ItemWithStatus> itemList =
      ImmutableList.builder();

  public StatusPerItemActionData addItem(ItemWithStatus item) {
    this.itemList.add(item);
    return this;
  }

  public ImmutableList<ItemWithStatus> itemList() {
    return this.itemList.build();
  }

  public ImmutableList<ItemWithStatus> createdItemList() {
    ImmutableList.Builder<ItemWithStatus> builder = ImmutableList.builder();
    for (ItemWithStatus item : itemList()) {
      if (item.status() == ActionStatus.CREATED) builder.add(item);
    }
    return builder.build();
  }

  public Multimap<String, ItemWithStatus> itemMap() {
    ImmutableMultimap.Builder<String, ItemWithStatus> builder =
        ImmutableMultimap.builder();
    for (ItemWithStatus item : itemList()) {
      builder.put(item.id(), item);
    }
    return builder.build();
  }
}
