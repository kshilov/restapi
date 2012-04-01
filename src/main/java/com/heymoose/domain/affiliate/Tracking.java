package com.heymoose.domain.affiliate;

import com.google.common.base.Optional;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.api.Api.appendQueryParam;
import com.sun.corba.se.spi.orbutil.fsm.ActionBase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.pkcs11.wrapper.CK_LOCKMUTEX;

@Singleton
public class Tracking {

  private final static Logger log = LoggerFactory.getLogger(Tracking.class);

  private final Repo repo;
  private final Accounts accounts;

  @Inject
  public Tracking(Repo repo, Accounts accounts) {
    this.repo = repo;
    this.accounts = accounts;
  }

  @Transactional
  public ShowStat track(@Nullable Long bannerId, Offer offer, User affiliate,
                        @Nullable String subId, @Nullable String sourceId) {
    ShowStat stat = findStat(bannerId, offer, affiliate, subId, sourceId);
    if (stat != null) {
      stat.inc();
      return stat;
    }
    stat = new ShowStat(bannerId, offer.id(), affiliate.id(), subId, sourceId);
    repo.put(stat);
    return stat;
  }
  
  private ShowStat findStat(Long bannerId, Offer offer, User affiliate, @Nullable String subId, @Nullable String sourceId) {
    DetachedCriteria criteria = DetachedCriteria.forClass(ShowStat.class)
        .add(Restrictions.eq("offer", offer))
        .add(Restrictions.eq("affiliate", affiliate));
    if (bannerId != null)
      criteria.add(Restrictions.eq("bannerId", bannerId));
    if (subId != null)
      criteria.add(Restrictions.eq("subId", subId));
    if (sourceId != null)
      criteria.add(Restrictions.eq("sourceId", sourceId));
    return repo.byCriteria(criteria);
  }

  @Transactional
  public Click click(@Nullable Long bannerId, long offerId, long affId,
                     @Nullable String subId, @Nullable String sourceId) {
    Click click = findClick(bannerId, offerId, subId, sourceId);
    if (click == null) {
      click = new Click(bannerId, offerId, affId, subId, sourceId);
      repo.put(click);
    }
    PayMethod payMethod = payMethod(repo.get(Offer.class, offerId));
    if (payMethod == PayMethod.CPC) {
      Offer offer = repo.get(Offer.class, offerId);
      accounts.lock(offerAccount(offer), click.affiliate().developerAccount());
      BigDecimal cost = cost(offer);
      BigDecimal amount = cost.multiply(new BigDecimal((100 - click.affiliate().fee())  / 100.0));
      AccountTx tx = accounts.transferCompact(offerAccount(offer), click.affiliate().developerAccount(), amount);
      tx.approve();
    }
    return click;
  }

  private static Account offerAccount(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).account();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).parent().account();
    else
      throw new IllegalStateException();
  }

  public Click findClick(@Nullable Long bannerId, long offerId, @Nullable String subId, @Nullable String sourceId) {
    DetachedCriteria criteria = DetachedCriteria.forClass(Click.class)
        .add(Restrictions.eq("offerId", offerId));
    if (bannerId != null)
      criteria.add(Restrictions.eq("bannerId", bannerId));
    if (subId != null)
      criteria.add(Restrictions.eq("subId", subId));
    if (sourceId != null)
      criteria.add(Restrictions.eq("sourceId", sourceId));
    return repo.byCriteria(criteria);
  }

  @Transactional
  public List<OfferAction> actionDone(Click click, String transactionId, Map<Offer, Optional<Double>> offers) {
    List<OfferAction> actions = newArrayList();
    for (Offer offer : offers.keySet()) {
      OfferGrant grant = granted(offer, click.affiliate());
      if (grant == null)
        throw new IllegalStateException("Offer not granted: " + offer.id());
      CpaPolicy cpaPolicy = cpaPolicy(offer);
      PayMethod payMethod = payMethod(offer);
      if (payMethod != PayMethod.CPA)
        throw new IllegalArgumentException("Not CPA offer: " + offer.id());
      BigDecimal cost = null;
      if (cpaPolicy == CpaPolicy.PERCENT) {
        Optional<Double> price = offers.get(offer);
        if (!price.isPresent())
          throw new IllegalArgumentException("No price for offer with id = " + offer.id());
        cost = new BigDecimal(price.get()).multiply(percent(offer).divide(new BigDecimal(100.0)));
      } else if (cpaPolicy == CpaPolicy.FIXED) {
        cost = cost(offer);
      } else throw new IllegalStateException();
      BigDecimal amount = cost.multiply(new BigDecimal((100 - click.affiliate().fee())  / 100.0));
      accounts.lock(offerAccount(offer), click.affiliate().developerAccount());
      AccountTx tx = accounts.transferCompact(offerAccount(offer), click.affiliate().developerAccount(), amount);
      OfferAction action = new OfferAction(click, offer, transactionId, tx);
      repo.put(action);
      try {
        getRequest(makeFullPostBackUri(URI.create(grant.postBackUrl()), click.sourceId(), click.subId(), offer.id()));
      } catch (Exception e) {
        log.warn("Error while requesting postBackUrl: " + grant.postBackUrl());
      }
      actions.add(action);
    }
    return actions;
  }

  private static URI makeFullPostBackUri(URI uri, String sourceId, String subId, long offerId) {
    uri = appendQueryParam(uri, "source_id", sourceId);
    uri = appendQueryParam(uri, "sub_id", subId);
    uri = appendQueryParam(uri, "offer_id", offerId);
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
  
  private static CpaPolicy cpaPolicy(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).cpaPolicy();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).cpaPolicy();
    else
      throw new IllegalArgumentException();
  }
  
  private static PayMethod payMethod(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).payMethod();
    else if (offer instanceof SubOffer)
      return PayMethod.CPA;
    else
      throw new IllegalArgumentException();
  }

  private static BigDecimal cost(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).cost();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).cost();
    else
      throw new IllegalArgumentException();
  }
  
  private static BigDecimal percent(Offer offer) {
    if (offer instanceof NewOffer)
      return ((NewOffer) offer).percent();
    else if (offer instanceof SubOffer)
      return ((SubOffer) offer).percent();
    else
      throw new IllegalArgumentException();
  }

  @Transactional
  public OfferGrant granted(Offer offer, User affiliate) {
    OfferGrant grant = repo.byHQL(
        OfferGrant.class,
        "from OfferGrant where offer = ? and affiliate = ?",
        offer, affiliate
    );
    if (grant == null)
      return null;
    if (!grant.offerIsVisible())
      return null;
    return grant;
  }
}
