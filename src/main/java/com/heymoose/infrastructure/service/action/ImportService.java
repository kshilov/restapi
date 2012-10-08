package com.heymoose.infrastructure.service.action;

import java.util.concurrent.TimeUnit;

public interface ImportService {

  ImportService forOffer(Long offerId);
  ImportService loadDataFromUrl(String url);
  ImportService loadEvery(Integer period, TimeUnit timeUnit);
  ImportService start();
  ImportService stop();

}
