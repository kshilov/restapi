package com.heymoose.infrastructure.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.errorinfo.ErrorInfo;
import com.heymoose.domain.errorinfo.ErrorInfoRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
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
  public List<ErrorInfo> list(int offset, int limit,
                              DateTime from, DateTime to) {
    Criteria criteria = sessionProvider.get().createCriteria(ErrorInfo.class);

    return criteria.add(Restrictions.ge("lastOccurred", from))
        .add(Restrictions.lt("lastOccurred", to))
        .addOrder(Order.desc("lastOccurred"))
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  public Long count(DateTime from, DateTime to) {
    return (Long) sessionProvider.get().createCriteria(ErrorInfo.class)
        .add(Restrictions.ge("lastOccurred", from))
        .add(Restrictions.lt("lastOccurred", to))
        .setProjection(Projections.count("id"))
        .uniqueResult();
  }

  @Override
  public boolean track(String uri, DateTime date, Throwable cause) {
    Session session = sessionProvider.get();
    ErrorInfo errorInfo = ErrorInfo.fromException(uri, date, cause);
    String sql = "update ErrorInfo " +
        "set occurrenceCount = occurrenceCount + 1, " +
        "lastOccurred = :date " +
        "where uri = :uri " +
        "and description = :description";
    int rowsAffected = session.createQuery(sql)
        .setParameter("date", errorInfo.lastOccurred())
        .setString("uri", errorInfo.uri())
        .setParameter("description", errorInfo.description())
        .executeUpdate();
    if (rowsAffected != 0) {
      return false;
    }
    session.save(errorInfo);
    return true;
  }
}
