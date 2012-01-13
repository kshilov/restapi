package com.heymoose.domain.hiber;

import com.heymoose.domain.OfferShow;
import com.heymoose.domain.OfferShowRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

@Singleton
public class OfferShowRepositoryHiber extends RepositoryHiber<OfferShow> implements OfferShowRepository {

  @Inject
  public OfferShowRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<OfferShow> getEntityClass() {
    return OfferShow.class;
  }
  
  @Override
  public Iterable<OfferShow> list(DateTime from, DateTime to,
      Long offerId, Long appId, Long performerId) {
    Criteria criteria = hiber()
        .createCriteria(getEntityClass())
        .add(Restrictions.between("showTime", from, to));
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (appId != null)
      criteria.add(Restrictions.eq("app.id", appId));
    if (performerId != null)
      criteria.add(Restrictions.eq("performer.id", performerId));

    return criteria.list();
  }
}
