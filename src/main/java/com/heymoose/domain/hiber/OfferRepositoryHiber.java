package com.heymoose.domain.hiber;

import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import java.math.BigInteger;
import java.util.Collections;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

@Singleton
public class OfferRepositoryHiber extends RepositoryHiber<Offer> implements OfferRepository {

  private final BannerSizeRepository bannerSizes;

  @Inject
  public OfferRepositoryHiber(Provider<Session> sessionProvider, BannerSizeRepository bannerSizes) {
    super(sessionProvider);
    this.bannerSizes = bannerSizes;
  }

  @Override
  public Set<Offer> availableFor(Performer performer, Filter filter) {
    List<Long> ids = newArrayList();
    for (Filter.Entry entry : filter.entries)
      ids.addAll(availableIdsFor(performer, entry));
    return loadByIds(ids);
  }

  private List<Long> availableIdsFor(Performer performer, Filter.Entry condition) {
    String sql =
        "select offer_id " +
        "from offer " +
        "join offer_order ord on ord.offer_id = offer.id " +
        "join targeting trg on trg.id = ord.targeting_id " +
        "where " +
        "(offer.id not in (select action.offer_id from action where performer_id = :performerId and action.done = true) or offer.reentrant = true) " +
        "and ((select allow_negative_balance = true from account where id = ord.account_id) " +
        "or (select ord.cpa <= balance from account_tx where account_tx.account_id = ord.account_id order by version desc limit 1)) " +
        "and ord.disabled = false " +
        "and (trg.male is null or trg.male = " + (performer.male() != null ? ":performerMale" : "null") + ") " +
        "and (trg.min_age is null or trg.min_age <= " + (performer.year() != null ? ":performerAge" : "null") + ") " +
        "and (trg.max_age is null or trg.max_age >= " + (performer.year() != null ? ":performerAge" : "null") + ") " +
        "and offer.type = :type ";
    if (condition.type == Offer.Type.BANNER) {
      Filter.BannerEntry bannerCondition = (Filter.BannerEntry) condition;
      BannerSize bannerSize = bannerSizes.byWidthAndHeight(bannerCondition.width, bannerCondition.height);
      if (bannerCondition == null)
        return emptyList();
      sql += String.format("and offer.size = %d ", bannerSize.id());
    }
    sql += "order by offer.creation_time desc limit " + condition.count;

    Query query = hiber()
        .createSQLQuery(sql)
        .setParameter("performerId", performer.id())
        .setParameter("type", condition.type.ordinal());
    
    if (performer.male() != null)
      query.setParameter("performerMale", performer.male());
    if (performer.year() != null)
      query.setParameter("performerAge", performer.year() != null ? DateTime.now().getYear() - performer.year() : null);
    
    List<BigInteger> ids = (List<BigInteger>) query.list();
    return longs(ids);
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
  public Offer byId(long id) {
    return (Offer) hiber()
        .createQuery("select offer from Offer as offer inner join offer.order as order where order.disabled = false and offer.id = :id")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  public Set<Offer> all() {
    return Sets.newHashSet(hiber()
        .createQuery("from Offer where order.disabled = false")
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
        .createQuery("from Offer as offer inner join fetch offer.order where offer.id in :ids order by offer.creationTime desc")
        .setParameterList("ids", ids)
        .list();
    return Sets.newHashSet(offers);
  }
}
