package com.heymoose.domain.hiber;

import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerInfo;
import java.math.BigInteger;
import java.util.Collections;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

@Singleton
public class OfferRepositoryHiber extends RepositoryHiber<Offer> implements OfferRepository {

  private final BannerSizeRepository bannerSizes;
  private final String randFunction;

  @Inject
  public OfferRepositoryHiber(Provider<Session> sessionProvider, BannerSizeRepository bannerSizes,
                              @Named("rand") String randFunction) {
    super(sessionProvider);
    this.bannerSizes = bannerSizes;
    this.randFunction = randFunction;
  }

  @Override
  public Set<Offer> availableFor(PerformerInfo info, Filter filter) {
    List<Long> ids = newArrayList();
    for (Filter.Entry entry : filter.entries)
      ids.addAll(availableIdsFor(info, entry));
    return loadByIds(ids);
  }

  private List<Long> availableIdsFor(PerformerInfo info, Filter.Entry condition) {

    String sql = "select offer_id " +
        "from offer " +
        "        join offer_order ord on ord.offer_id = offer.id " +
        "        join targeting trg on trg.id = ord.targeting_id " +
        "where " +
        "(offer.id not in (select action.offer_id from action where performer_id = :performer and action.done = true) or offer.reentrant = true) " +
        "and ( " +
        "        (select allow_negative_balance = true from account where id = ord.account_id) " +
        "        or (select ord.cpa <= balance from account_tx where account_tx.account_id = ord.account_id order by version desc limit 1) " +
        ") " +
        "and ord.disabled = false ";

    if (info.performer.male() != null)
      sql += "and (trg.male is null or trg.male = :performerMale) ";

    if (info.performer.year() != null)
        sql += "and (trg.min_age is null or trg.min_age <= :performerAge) " +
        "and (trg.max_age is null or trg.max_age >= :performerAge) ";

    if (info.city != null)
        sql += "and ( " +
        "        trg.cities_filter_type is null " +
        "        or ((trg.cities_filter_type = 0 and trg.id in ( " +
        "                select targeting_id " +
        "                from targeting_city tc left join city on tc.city_id = city.id " +
        "                where city.name like :city " +
        "        )) " +
        "        and (trg.cities_filter_type = 1 and trg.id not in ( " +
        "                select targeting_id " +
        "                from targeting_city tc left join city on tc.city_id = city.id " +
        "                where city.name like :city " +
        "        ))) " +
        ") ";

    sql += "and offer.type = :type ";

    BannerSize bannerSize = null;
    if (condition.type == Offer.Type.BANNER) {
      Filter.BannerEntry bannerCondition = (Filter.BannerEntry) condition;
      bannerSize = bannerSizes.byWidthAndHeight(bannerCondition.width, bannerCondition.height);
      if (bannerSize == null)
        return emptyList();
      sql += "and offer.size = :bannerSize ";
    }
    sql += "order by " + randFunction + " limit :limit";

    Query query = hiber()
        .createSQLQuery(sql)
        .setParameter("performer", info.performer.id())
        .setParameter("type", condition.type.ordinal());

    if (info.performer.male() != null)
      query.setParameter("performerMale", info.performer.male());

    if (info.performer.year() != null)
      query.setParameter("performerAge", DateTime.now().getYear() - info.performer.year());

    if (info.city != null)
      query.setParameter("city", info.city);

    if (bannerSize != null)
      query.setParameter("bannerSize", bannerSize.id());

    query.setParameter("limit", condition.count);

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
        .createQuery("from Offer as offer inner join fetch offer.order where offer.id in :ids order by " + randFunction)
        .setParameterList("ids", ids)
        .list();
    return Sets.newHashSet(offers);
  }
}
