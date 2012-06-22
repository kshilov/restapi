package com.heymoose.resource;

import com.heymoose.domain.affiliate.ErrorInfo;
import com.heymoose.domain.affiliate.ErrorInfoRepository;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlErrorsInfo;
import org.joda.time.DateTime;

import java.util.List;

public final class ErrorInfoResource {

  private final ErrorInfoRepository repository;

  public ErrorInfoResource(ErrorInfoRepository repository) {
    this.repository = repository;
  }

  public XmlErrorsInfo list(int offset, int limit, Long affId,
                            Long startTime, Long endTime) {
    DateTime dateFrom = new DateTime(startTime);
    DateTime dateTo = new DateTime(endTime);
    List<ErrorInfo> result = repository.list(offset, limit, affId, dateFrom, dateTo);
    return Mappers.toXmlErrorsInfo(result);
  }
}
