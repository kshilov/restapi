package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.action.ActionData;

public interface ImportService<T extends ActionData> {

  ImportService setUrl(String url);
  ImportService setParentOfferId(Long parentOfferId);
  ImportService setImportPeriod(Integer minutes);
  ImportService start();
  ImportService stop();

}
