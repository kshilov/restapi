package com.heymoose.domain.affiliate;

import org.joda.time.DateTime;

import java.util.List;

public interface ErrorInfoRepository {

  List<ErrorInfo> list(int offset, int limit, Long affiliateId,
                              DateTime from, DateTime to);

  /**
   * Track error.
   * <br/>
   * <ul>
   * <li>
   *   If error with given `id` (see {@link ErrorInfo}) already exists,
   *   should increment {@link com.heymoose.domain.affiliate.ErrorInfo#occurrenceCount()}.
   * </li>
   * <li>
   *   If it doesn't exist - new record should be created.
   * </li>
   * </ul>
   *
   * @return true if error is new, false otherwise
   */
  boolean track(Long affiliateId, String uri, DateTime date, Throwable cause);
}
