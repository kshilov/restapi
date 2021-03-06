package com.heymoose.infrastructure.persistence;

import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.offer.SubOfferRepository;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@Singleton
public class SubOfferRepositoryHiber extends RepositoryHiber<SubOffer> implements
    SubOfferRepository {

  @Inject
  public SubOfferRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }
  
  @Override
  public Iterable<SubOffer> list(long parentId) {
    return hiber()
        .createCriteria(SubOffer.class)
        .add(Restrictions.eq("parent.id", parentId))
        .addOrder(Order.asc("id"))
        .list();
  }

  @Override
  public long count(long parentId) {
    return Long.parseLong(hiber()
        .createCriteria(SubOffer.class)
        .add(Restrictions.eq("parent.id", parentId))
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  @Override
  protected Class<SubOffer> getEntityClass() {
    return SubOffer.class;
  }

}
