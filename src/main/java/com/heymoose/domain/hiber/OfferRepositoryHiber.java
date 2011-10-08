package com.heymoose.domain.hiber;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Singleton
public class OfferRepositoryHiber extends RepositoryHiber<Offer> implements OfferRepository {

  @Inject
  public OfferRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Set<Offer> availableFor(long performerId) {
    // TODO: add ordering
    String sql = "select offer.id " +
        "from offer " +
        "inner join offer_order on offer_order.offer_id = offer.id " +
        "where " +
        "offer.id not in (select action.offer_id from action where performer_id = :performerId and action.deleted = false) " +
        "and (select offer_order.cpa <= balance from account_tx where account_tx.account_id = offer_order.account_id order by version desc limit 1) " +
        "and offer_order.deleted = false  " +
        "and offer_order.approved = true";
    List<BigInteger> ids = (List<BigInteger>) hiber()
        .createSQLQuery(sql)
        .setParameter("performerId", performerId)
        .list();
    return loadByIds(longs(ids));
  }

  @Override
  public Set<Offer> doneFor(long performerId) {
    String sql = "select " +
        "offer.id " +
        "from " +
        "offer " +
        "right join action on offer.id = action.offer_id " +
        "where action.performer_id = :performerId";
    List<BigInteger> ids = (List<BigInteger>) hiber()
            .createSQLQuery(sql)
            .setParameter("performerId", performerId)
            .list();
    return loadByIds(longs(ids));
  }

  @Override
  public Set<Offer> approved() {
    String sql = "select " +
        "offer.id " +
        "from offer inner join offer_order on offer.id = offer_order.offer_id " +
        "where offer_order.deleted = false and offer_order.approved = true";
    List<BigInteger> ids = (List<BigInteger>) hiber()
            .createSQLQuery(sql)
            .list();
    return loadByIds(longs(ids));
  }

  @Override
  public Offer byId(long id) {
    return (Offer) hiber()
        .createQuery("from Offer where order.deleted = false and id = :id")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  public Set<Offer> all() {
    return Sets.newHashSet(hiber()
        .createQuery("from Offer where order.deleted = false")
        .list());
  }

  @Override
  protected Class<Offer> getEntityClass() {
    return Offer.class;
  }

  private static List<Long> longs(List<BigInteger> from) {
    List<Long> longs = Lists.newArrayListWithCapacity(from.size());
    for (BigInteger x : from)
      longs.add(x.longValue());
    return longs;
  }

  private Set<Offer> loadByIds(List<Long> ids) {
    if (ids.isEmpty())
      return Collections.emptySet();
    List<Offer> offers = hiber()
        .createQuery("from Offer where id in :ids")
        .setParameterList("ids", ids)
        .list();
    return Sets.newHashSet(offers);
  }
}
