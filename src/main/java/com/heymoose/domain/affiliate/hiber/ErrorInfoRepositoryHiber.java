package com.heymoose.domain.affiliate.hiber;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.affiliate.ErrorInfo;
import com.heymoose.domain.affiliate.ErrorInfoRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.inject.Provider;
import java.util.List;

@Singleton
public final class ErrorInfoRepositoryHiber implements ErrorInfoRepository {

  private final Provider<Session> sessionProvider;

  @Inject
  public ErrorInfoRepositoryHiber(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ErrorInfo> list(int offset, int limit, Long affiliateId,
                              DateTime from, DateTime to) {
    Criteria criteria = sessionProvider.get().createCriteria(ErrorInfo.class);

    return criteria.add(Restrictions.eq("key.affiliateId", affiliateId))
        .add(Restrictions.ge("lastOccurred", from))
        .add(Restrictions.lt("lastOccurred", to))
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public boolean track(Long affiliateId, String uri, DateTime date,
                       Throwable cause) {
    Session session = sessionProvider.get();
    ErrorInfo errorInfo = ErrorInfo.fromException(affiliateId, uri, date, cause);
    ErrorInfo savedError = (ErrorInfo) session.get(
        ErrorInfo.class, errorInfo.id());
    if (savedError != null) {
      errorInfo.setOccurrenceCount(savedError.occurrenceCount() + 1);
      savedError.fillFromErrorInfo(errorInfo);
      session.update(savedError);
      return false;
    }
    session.save(errorInfo);
    return true;
  }
}
