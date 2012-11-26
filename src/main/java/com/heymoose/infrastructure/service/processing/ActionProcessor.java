package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.Placement;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.service.Sites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.*;

@Singleton
public final class ActionProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(ActionProcessor.class);

  protected Repo repo;
  protected Accounting accounting;
  protected OfferGrantRepository offerGrants;
  protected Sites sites;

  @Inject
  protected ActionProcessor(Repo repo, Accounting accounting,
                            OfferGrantRepository offerGrants,
                            Sites sites) {
    this.repo = repo;
    this.accounting = accounting;
    this.offerGrants = offerGrants;
    this.sites = sites;
  }


  public void process(ProcessableData data) {
    log.info("Entering processing: {}", data);
    Token token = data.token();
    BaseOffer offer = data.offer();
    String transactionId = data.transactionId();
    OfferStat source = token.stat();
    String postbackUrl;
    if (data.site() != null) {
      Placement placement = sites.checkPermission(data.offer(), data.site());
      postbackUrl = placement.postBackUrl();
    } else {
      OfferGrant grant = offerGrants.checkGrant(source.affiliate(), offer);
      postbackUrl = grant.postBackUrl();
    }

    if (!offer.reentrant()) {
      OfferAction existed = findAction(repo, offer, token, transactionId);
      if (existed != null) {
        log.warn("Non-reentrant offer {} with existing action {}. Skipping..",
            offer, existed);
        throw new IllegalStateException("Offer is not reentrant. " +
            "Action exists: " + existed.id());
      }
    }


    Tariff tariff = data.offer().tariff();
    if (offer instanceof Offer && ((Offer) offer).isProductOffer()
        && data.product() != null) {
      tariff = data.product().tariff();
    }

    log.debug("Processing tariff {} chosen for data {}", tariff, data);
    BigDecimal advertiserCharge;
    switch (tariff.cpaPolicy()) {
      case FIXED:
        advertiserCharge = tariff.cost();
        break;
      case PERCENT:
        if (data.price() == null) {
          log.warn("No price given. Can't add action for percent offer. {}",
              data);
          return;
        }
        advertiserCharge = tariff.percentOf(data.price());
        break;
      case DOUBLE_FIXED:
        OfferAction exists = findAction(repo, offer, token);
        if (exists == null) {
          advertiserCharge = tariff.firstActionCost();
        } else {
          advertiserCharge = tariff.otherActionCost();
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown cpa policy. " +
            tariff.cpaPolicy());
    }

    if (advertiserCharge.signum() <= 0) {
      log.warn("Advertiser charge could not be calculated. " +
          "Skipping {}", data);
      return;
    }
    BigDecimal affiliatePart = tariff.affiliatePart(advertiserCharge);
    BigDecimal heymoosePart = tariff.heymoosePart(advertiserCharge);
    OfferStat stat = copyStat(source, offer)
        .setProduct(data.product())
        .setSiteId(data.site() == null ? null : data.site().id())
        .addToNotConfirmedRevenue(affiliatePart)
        .addToNotConfirmedFee(heymoosePart);
    if (tariff.cpaPolicy() == CpaPolicy.PERCENT) {
      stat.incSales();
    } else {
      stat.incLeads();
    }
    stat.incActions();
    repo.put(stat);
    OfferAction action = new OfferAction(token, source.affiliate(), stat,
        source, offer, transactionId);
    action.setProduct(data.product());
    action.setPurchasePrice(data.price());
    action.setSite(data.site());
    repo.put(action);
    accounting.notConfirmedActionPayments(action, affiliatePart, heymoosePart);
    log.info("Tracked conversion of data: {}:\n" +
        "   advertiser charge:  {}\n" +
        "   affiliate money:    {}\n" +
        "   heymoose fee:       {}\n" +
        "   action id:          {}",
        new Object[] { data,
            advertiserCharge, affiliatePart, heymoosePart, action.id() } );

    doPostBack(postbackUrl, action);
    data.setOfferAction(action);
  }
}

