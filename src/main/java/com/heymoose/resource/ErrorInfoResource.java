package com.heymoose.resource;

import com.heymoose.domain.affiliate.ErrorInfoRepository;

public final class ErrorInfoResource {

  private final ErrorInfoRepository repository;

  public ErrorInfoResource(ErrorInfoRepository repository) {
    this.repository = repository;
  }

  public void list(int offset, int limit, Long affId) {
    repository.list(offset, limit, affId);
  }
}
