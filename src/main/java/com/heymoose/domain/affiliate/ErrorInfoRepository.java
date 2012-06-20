package com.heymoose.domain.affiliate;

import java.util.List;

public interface ErrorInfoRepository {

  public List<ErrorInfo> list(int offset, int limit, Long affiliateId);
}
