package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.action.ActionData;

public interface ActionDataImport<T extends ActionData> {

  ActionDataImport setUrl(String url);
  ActionDataImport setParentOfferId(Long parentOfferId);
  ActionDataImport setImportPeriod(Integer minutes);
  ActionDataImport start();
  ActionDataImport stop();

}
