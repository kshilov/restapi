package com.heymoose.domain.hiber;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import static com.google.common.collect.Iterables.isEmpty;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
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
import static java.lang.Math.round;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.checkedCollection;
import static java.util.Collections.emptyList;
import static java.util.Collections.indexOfSubList;
import static java.util.Collections.min;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    List<Long> ids = newArrayList();
    for (Filter.Entry entry : filter.entries) {
      for (long offerId : availableIdsFor(performer, entry, context)) {
        mapping.put(offerId, entry);
        ids.add(offerId);
      }
    }
    return load(ids, mapping);
  }

  private Map<Long, Double> getOfferCTRs(long appId) {
    String sql ="select a.offer_id, cast(a.c as decimal) / s.c  " +
        "from ( " +
        "select offer_id offer_id, count(*) c " +
        "from action " +
        "where creation_time >= :start and app_id = :appId " +
        "group by offer_id " +
        ") a left join ( " +
        "select offer_id, count(*) c " +
        "from offer_show " +
        "where show_time >= :start and app_id = :appId " +
        "group by offer_id " +
        ") s " +
        "on a.offer_id = s.offer_id";
    List<Object[]> records = hiber().createSQLQuery(sql)
        .setParameter("start", DateTime.now().minusDays(1).toDate())
        .setParameter("appId", appId)
        .list();
    Map<Long, Double> ret = newHashMap();
    for (Object[] record : records) {
      long offerId = ((BigInteger) record[0]).longValue();
      BigDecimal _ctr = (BigDecimal) record[1];
      Double ctr = (_ctr == null) ? null : _ctr.doubleValue();
      ret.put(offerId, ctr);
    }
    return ret;
  }

  public static class BannerContainer {
    public final Long bannerId;
    public final double cpc;
    public Double ctr;

    public double product;
    public int weight;
    public int sum;

    public BannerContainer(Long bannerId, double cpc, Double ctr) {
      this.bannerId = bannerId;
      this.cpc = cpc;
      this.ctr = ctr;
    }
  }

  public static void calcWeights(Iterable<BannerContainer> banners) {
    Double minPositiveCtr = Double.MAX_VALUE;
    boolean found = false;
    for (BannerContainer banner : banners) {
      if (banner.ctr == null)
        continue;
      if (minPositiveCtr > banner.ctr) {
        minPositiveCtr = banner.ctr;
        found = true;
      }
    }
    if (!found) {
      for (BannerContainer banner : banners)
        banner.weight = 1;
      return;
    }
    for (BannerContainer banner: banners) {
      if (banner.ctr == null)
        banner.product = banner.cpc * minPositiveCtr / 2;
      else
        banner.product = banner.cpc * banner.ctr;
    }
    double minProduct = Double.MAX_VALUE;
    for (BannerContainer banner : banners)
      if (minProduct > banner.product)
        minProduct = banner.product;
    for (BannerContainer banner : banners)
      banner.weight = Long.valueOf(round(banner.product / minProduct)).intValue();
  }

  public static List<Long> extractRandoms(Collection<BannerContainer> banners, int count) {
    checkArgument(count >= 0);
    if (count == 0 || isEmpty(banners))
      return emptyList();
    int sum = 0;
    for (BannerContainer banner : banners) {
      sum += banner.weight;
      banner.sum = sum;
    }
    ImmutableList.Builder<Long> randoms = ImmutableList.builder();
    Random random = new Random();
    for (int i = 0; i < count && i < banners.size(); i++) {
      int val = 1 + random.nextInt(sum);
      for (BannerContainer banner : banners) {
        if (banner.sum >= val) {
          randoms.add(banner.bannerId);
          break;
        }
      }
    }
    return randoms.build();
  }

  private List<Long> availableIdsFor(Performer performer, Filter.Entry condition, Context context) {

    Map<Long, Double> ctrs = getOfferCTRs(context.app.id());

    String sql = "select offer_id, ord.cpa " +
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
      		"(trg.min_hour < trg.max_hour and :hour >= trg.min_hour and :hour <= trg.max_hour) or " +
      		"(trg.min_hour >= trg.max_hour and (:hour >= trg.min_hour or :hour <= trg.max_hour))) ";

    sql += "and offer.type = :type ";

    BannerSize bannerSize = null;
    if (condition.type == Offer.Type.BANNER) {
      Filter.BannerEntry bannerCondition = (Filter.BannerEntry) condition;
      bannerSize = bannerSizes.byWidthAndHeight(bannerCondition.width, bannerCondition.height);
      if (bannerSize == null)
        return emptyList();
      sql += "and offer.id in (select offer_id from banner where size = :bannerSize and offer_id = offer.id) ";
    }

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

    List<Object[]> records = query.list();
    Set<BannerContainer> banners = newHashSet();
    for (Object[] record : records) {
      long bannerId = ((BigInteger) record[0]).longValue();
      double cpc = ((BigDecimal) record[1]).doubleValue();
      Double ctr = ctrs.get(bannerId);
      banners.add(new BannerContainer(bannerId, cpc, ctr));
    }

    calcWeights(banners);

    return extractRandoms(banners, condition.count);
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

  private Set<OfferData> load(List<Long> ids, Map<Long, Filter.Entry> mapping) {
    if (mapping.isEmpty())
      return Collections.emptySet();
    List<Offer> offers = hiber()
        .createQuery("from Offer as offer inner join fetch offer.order where offer.id in :ids order by " + randFunction)
        .setParameterList("ids", ids)
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
