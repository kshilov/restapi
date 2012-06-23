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

import static com.heymoose.domain.affiliate.ErrorInfo.*;

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

    return criteria.add(Restrictions.eq(AFFILIATE_ID, affiliateId))
        .add(Restrictions.ge(LAST_OCCURRED, from))
        .add(Restrictions.lt(LAST_OCCURRED, to))
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
}
