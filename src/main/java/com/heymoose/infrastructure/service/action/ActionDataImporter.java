package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.action.ActionData;

import java.util.List;

public interface ActionDataImporter<T extends ActionData> {

  void doImport(List<T> actionList, Long parentOfferId);
  void doImport(T action, Long parentOfferId);
}
