package com.heymoose.domain.affiliate;

import com.google.common.base.Optional;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.AdminAccountAccessor;
import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.affiliate.counter.BufferedClicks;
import com.heymoose.domain.affiliate.counter.BufferedShows;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.QueryUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Tracking {

  private final static Logger log = LoggerFactory.getLogger(Tracking.class);

  private final Repo repo;
  private final AdminAccountAccessor adminAccountAccessor;
  private final Accounting accounting;
  private final BufferedShows bufferedShows;
  private final BufferedClicks bufferedClicks;

  @Inject
  public Tracking(Repo repo, AdminAccountAccessor adminAccountAccessor, Accounting accounting, BufferedShows bufferedShows, BufferedClicks bufferedClicks) {
    this.repo = repo;
    this.adminAccountAccessor = adminAccountAccessor;
    this.accounting = accounting;
    this.bufferedShows = bufferedShows;
    this.bufferedClicks = bufferedClicks;
  }

  @Transactional
  public OfferStat track(@Nullable Long bannerId, long offerId, long master, long affId,
                        @Nullable String subId, @Nullable String sourceId) {
    OfferStat stat = findStat(bannerId, offerId, affId, subId, sourceId);
    if (stat != null) {
      bufferedShows.inc(stat.id());
      return stat;
    }
    stat = new OfferStat(bannerId, offerId, master, affId, subId, sourceId);
    stat.incShows();
    repo.put(stat);
    return stat;
  }

  @Transactional
  public String click(@Nullable Long bannerId, long offerId, long master, long affId,
                     @Nullable String subId, @Nullable String sourceId) {
    OfferStat stat = findStat(bannerId, offerId, affId, subId, sourceId);
    if (stat == null) {
      stat = new OfferStat(bannerId, offerId, master, affId, subId, sourceId);
      stat.incClicks();
      repo.put(stat);
    } else {
      bufferedClicks.inc(stat.id());
    }
    Offer offer = repo.get(Offer.class, offerId);
    PayMethod payMethod = offer.payMethod();
    if (payMethod == PayMethod.CPC) {
      BigDecimal cost = offer.cost();
      BigDecimal amount = cost.multiply(new BigDecimal((100 - stat.affiliate().fee())  / 100.0));
      BigDecimal revenue = cost.subtract(amount);
      accounting.transferMoney(
          offer.account(),
          stat.affiliate().affiliateAccount(),
          amount,
          AccountingEvent.CLICK_CREATED,
          stat.id()
      );
      accounting.transferMoney(
          offer.account(),
          adminAccountAccessor.getAdminAccount(),
          revenue,
          AccountingEvent.CLICK_CREATED,
          stat.id()
      );
      stat.addToConfirmedRevenue(amount);
    }
    Token token = new Token(stat);
    repo.put(token);
    return token.value();
  }

  private OfferStat findStat(@Nullable Long bannerId, long offerId, long affId, @Nullable String subId, @Nullable String sourceId) {
    DetachedCriteria criteria = DetachedCriteria.forClass(OfferStat.class)
        .add(Restrictions.eq("offer.id", offerId))
        .add(Restrictions.eq("affiliate.id", affId));
    addEqOrIsNull(criteria, "bannerId", bannerId);
    addEqOrIsNull(criteria, "sourceId", sourceId);
    addEqOrIsNull(criteria, "subId", subId);
    criteria.add(Restrictions.ge("creationTime", DateTime.now().minusHours(1)));
    return repo.byCriteria(criteria);
  }

  private OfferAction findAction(BaseOffer offer, Token token) {
    return repo.byHQL(OfferAction.class, "from OfferAction where offer = ? and token = ?", offer, token);
  }

  private static DetachedCriteria addEqOrIsNull(DetachedCriteria criteria, String property, Object value) {
    if (value == null)
      criteria.add(Restrictions.isNull(property));
    else
      criteria.add(Restrictions.eq(property, value));
    return criteria;
  }

  @Transactional
  public List<OfferAction> actionDone(Token token, String transactionId, Map<BaseOffer, Optional<Double>> offers) {
    OfferStat source = token.stat();
    List<OfferAction> actions = newArrayList();
    for (BaseOffer offer : offers.keySet()) {
      OfferGrant grant = granted(offer, source.affiliate());
      if (grant == null)
        throw new IllegalStateException("Offer not granted: " + offer.id());
      OfferAction existent = findAction(offer, token);
      if (existent != null && (existent.transactionId().equals(transactionId) || !offer.reentrant()))
        continue;
      PayMethod payMethod = offer.payMethod();
      CpaPolicy cpaPolicy = offer.cpaPolicy();
      if (payMethod != PayMethod.CPA)
        throw new IllegalArgumentException("Not CPA offer: " + offer.id());
      BigDecimal cost;
      BigDecimal cost2 = null;
      if (cpaPolicy == CpaPolicy.PERCENT) {
        Optional<Double> price = offers.get(offer);
        if (!price.isPresent())
          throw new IllegalArgumentException("No price for offer with id = " + offer.id());
        cost = new BigDecimal(price.get()).multiply(offer.percent().divide(new BigDecimal(100.0)));
      } else if (cpaPolicy == CpaPolicy.FIXED) {
        cost = offer.cost();
      } else if (cpaPolicy == CpaPolicy.DOUBLE_FIXED) {
        cost = offer.cost();
        cost2 = offer.cost2();
      } else throw new IllegalStateException();
      if (existent != null && cost2 != null)
        cost = cost2;
      BigDecimal amount = cost.multiply(new BigDecimal((100 - source.affiliate().fee()) / 100.0));
      BigDecimal revenue = cost.subtract(amount);
      OfferStat stat = new OfferStat(source.bannerId(), offer.id(), offer.master(), source.affiliate().id(), source.subId(), source.sourceId());
      if (cpaPolicy == CpaPolicy.FIXED || cpaPolicy == CpaPolicy.DOUBLE_FIXED)
        stat.incLeads();
      if (cpaPolicy == CpaPolicy.PERCENT)
        stat.incSales();
      stat.addToNotConfirmedRevenue(amount);
      repo.put(stat);
      OfferAction action = new OfferAction(token, source.affiliate(), stat, source, offer, transactionId);
      repo.put(action);
      accounting.transferMoney(
          offer.account(),
          source.affiliate().affiliateAccountNotConfirmed(),
          amount,
          AccountingEvent.ACTION_CREATED,
          action.id()
      );
      accounting.transferMoney(
          offer.account(),
          adminAccountAccessor.getAdminAccountNotConfirmed(),
          revenue,
          AccountingEvent.ACTION_CREATED,
          action.id()
      );
      try {
        if (grant.postBackUrl() != null)
          getRequest(makeFullPostBackUri(URI.create(grant.postBackUrl()), source.sourceId(), source.subId(), offer.id()));
      } catch (Exception e) {
        log.warn("Error while requesting postBackUrl: " + grant.postBackUrl(), e);
      }
      actions.add(action);
    }
    return actions;
  }

  private static URI makeFullPostBackUri(URI uri, String sourceId, String subId, long offerId) {
    uri = QueryUtil.appendQueryParam(uri, "source_id", sourceId);
    uri = QueryUtil.appendQueryParam(uri, "sub_id", subId);
    uri = QueryUtil.appendQueryParam(uri, "offer_id", offerId);
    return uri;
  }

  private static void getRequest(URI uri) {
    InputStream is = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      URL url = new URL(uri.toString());
      is = url.openStream();
      IOUtils.copy(is, baos);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      if (is != null)
        try {
          is.close();
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
    }
  }

  @Transactional
  public OfferGrant granted(BaseOffer offer, User affiliate) {
    BaseOffer grantTarget;
    if (offer instanceof Offer)
      grantTarget = offer;
    else if (offer instanceof SubOffer)
      grantTarget = ((SubOffer) offer).parent();
    else
      throw new IllegalStateException();
    OfferGrant grant = repo.byHQL(
        OfferGrant.class,
        "from OfferGrant where offer = ? and affiliate = ?",
        grantTarget, affiliate
    );
    if (grant == null)
      return null;
    if (!grant.offerIsVisible())
      return null;
    return grant;
  }
}
