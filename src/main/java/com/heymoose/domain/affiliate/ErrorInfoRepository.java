package com.heymoose.domain.affiliate;

import java.util.List;

public interface ErrorInfoRepository {

  public List<Object> list(int offset, int limit, Long affilateId);
}
