package com.heymoose.domain.affiliate;

import org.joda.time.DateTime;

import java.util.List;

public interface ErrorInfoRepository {

  public List<ErrorInfo> list(int offset, int limit, Long affiliateId,
                              DateTime from, DateTime to);
}
