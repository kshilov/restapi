package com.heymoose.domain.hiber;

import com.google.common.collect.Lists;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Sets;
import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerRepository;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.domain.Context;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.VideoOffer;
import com.heymoose.resource.api.data.OfferData;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Map;
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
  private final BannerRepository banners;
  private final BigDecimal compensation;

  @Inject
  public OfferRepositoryHiber(Provider<Session> sessionProvider, BannerSizeRepository bannerSizes,
                              @Named("rand") String randFunction, BannerRepository banners,
                              @Named("compensation") BigDecimal compensation) {
    super(sessionProvider);
    this.bannerSizes = bannerSizes;
    this.randFunction = randFunction;
    this.banners = banners;
    this.compensation = compensation;
  }

  @Override
  public Set<OfferData> availableFor(Performer performer, Filter filter, Context context) {
    Map<Long, Filter.Entry> mapping = newHashMap();
    for (Filter.Entry entry : filter.entries) {
      for (long offerId : availableIdsFor(performer, entry, context))
        mapping.put(offerId, entry);
    }
    return load(mapping);
  }

  private List<Long> availableIdsFor(Performer performer, Filter.Entry condition, Context context) {

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

    if (performer.male() != null)
      sql += "and (trg.male is null or trg.male = :performerMale) ";

    if (performer.year() != null)
        sql += "and (trg.min_age is null or trg.min_age <= :performerAge) " +
        "and (trg.max_age is null or trg.max_age >= :performerAge) ";

    if (performer.city() != null)
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

    if (context.app != null)
      sql += "and ( " +
          "        trg.app_filter_type is null " +
          "        or ((trg.app_filter_type = 0 and trg.id in ( " +
          "                select targeting_id " +
          "                from targeting_app tc left join app on tc.app_id = app.id " +
          "                where app.id = :appId " +
          "        )) " +
          "        and (trg.app_filter_type = 1 and trg.id not in ( " +
          "                select targeting_id " +
          "                from targeting_app tc left join app on tc.app_id = app.id " +
          "                where app.id = :appId " +
          "        ))) " +
          ") ";

    if (context.hour != null)
      sql += " and (" +
      		"(trg.min_hour is null and trg.max_hour is null) or " +
      		"(trg.min_hour is null and :hour <= trg.max_hour) or " +
      		"(trg.max_hour is null and :hour >= trg.min_hour) or " +
      		"(trg.min_hour <= trg.max_hour and :hour >= trg.min_hour and :hour <= trg.max_hour) or " +
      		"(trg.min_hour > trg.max_hour and (:hour >= trg.min_hour or :hour <= trg.max_hour))) ";
    
      /*sql += " and (trg.min_hour is null or trg.min_hour <= :hour) " +
          " and (trg.max_hour is null or trg.max_hour >= :hour) ";*/

    sql += "and offer.type = :type ";

    BannerSize bannerSize = null;
    if (condition.type == Offer.Type.BANNER) {
      Filter.BannerEntry bannerCondition = (Filter.BannerEntry) condition;
      bannerSize = bannerSizes.byWidthAndHeight(bannerCondition.width, bannerCondition.height);
      if (bannerSize == null)
        return emptyList();
      sql += "and offer.id in (select offer_id from banner where size = :bannerSize and offer_id = offer.id) ";
    }
    sql += "order by " + randFunction + " limit :limit";

    Query query = hiber()
        .createSQLQuery(sql)
        .setParameter("performer", performer.id())
        .setParameter("type", condition.type.ordinal());

    if (performer.male() != null)
      query.setParameter("performerMale", performer.male());

    if (performer.year() != null)
      query.setParameter("performerAge", DateTime.now().getYear() - performer.year());

    if (performer.city() != null)
      query.setParameter("city", performer.city());

    if (bannerSize != null)
      query.setParameter("bannerSize", bannerSize.id());

    if (context.app != null)
      query.setParameter("appId", context.app.id());

    if (context.hour != null)
      query.setParameter("hour", context.hour);

    query.setParameter("limit", condition.count);

    List<BigInteger> ids = (List<BigInteger>) query.list();
    return longs(ids);
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

  private Set<OfferData> load(Map<Long, Filter.Entry> mapping) {
    if (mapping.isEmpty())
      return Collections.emptySet();
    List<Offer> offers = hiber()
        .createQuery("from Offer as offer inner join fetch offer.order where offer.id in :ids order by " + randFunction)
        .setParameterList("ids", mapping.keySet())
        .list();
    Set<OfferData> ret = newHashSet();
    for (Offer offer : offers)
      ret.add(convert(offer, mapping.get(offer.id())));
    return ret;
  }

  private OfferData convert(Offer offer, Filter.Entry filter) {
    if (offer instanceof BannerOffer) {
      BannerOffer bannerOffer = (BannerOffer) offer;
      Filter.BannerEntry bannerEntry = (Filter.BannerEntry) filter;
      BannerSize bannerSize = bannerSizes.byWidthAndHeight(bannerEntry.width, bannerEntry.height);
      return OfferData.toOfferData(bannerOffer, compensation, bannerSize);
    } else if (offer instanceof RegularOffer) {
      RegularOffer regularOffer = (RegularOffer) offer;
      return OfferData.toOfferData(regularOffer, compensation);
    } else if (offer instanceof VideoOffer) {
      VideoOffer videoOffer = (VideoOffer) offer;
      return OfferData.toOfferData(videoOffer, compensation);
    } else {
      throw new IllegalArgumentException("Unknown offer type: " + offer.getClass().getSimpleName());
    }
  }
}
