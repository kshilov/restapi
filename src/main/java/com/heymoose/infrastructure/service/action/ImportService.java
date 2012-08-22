package com.heymoose.infrastructure.service.action;

public interface ImportService {

  ImportService setUrl(String url);
  ImportService setParentOfferId(Long parentOfferId);
  ImportService setImportPeriod(Integer minutes);
  ImportService start();
  ImportService stop();

}
