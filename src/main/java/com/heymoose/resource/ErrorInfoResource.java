package com.heymoose.resource;

import com.heymoose.domain.affiliate.ErrorInfoRepository;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlErrorsInfo;

public final class ErrorInfoResource {

  private final ErrorInfoRepository repository;

  public ErrorInfoResource(ErrorInfoRepository repository) {
    this.repository = repository;
  }

  public XmlErrorsInfo list(int offset, int limit, Long affId) {
    return Mappers.toXmlErrorsInfo(repository.list(offset, limit, affId));
  }
}
