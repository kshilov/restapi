package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.tariff.Tariff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.*;

public final class ActionProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(ActionProcessor.class);

  protected Repo repo;
  protected Accounting accounting;
  protected OfferGrantRepository offerGrants;

  @Inject
  protected ActionProcessor(Repo repo, Accounting accounting,
                                  OfferGrantRepository offerGrants) {
    this.repo = repo;
    this.accounting = accounting;
    this.offerGrants = offerGrants;
  }


  public OfferAction process(ProcessableData data) {
    Token token = data.token();
    BaseOffer offer = data.offer();
    String transactionId = data.transactionId();
    OfferStat source = token.stat();
    OfferGrant grant = offerGrants.checkGrant(source.affiliate(), offer);

    OfferAction exists = checkIfActionExists(repo, offer, token, transactionId);


    Tariff tariff = data.offer().tariff();
    if (offer instanceof Offer && ((Offer) offer).isProductOffer()
        && data.product() != null) {
      tariff = data.product().tariff();
    }

    BigDecimal advertiserCharge = BigDecimal.ZERO;
    switch (tariff.cpaPolicy()) {
      case FIXED:
        advertiserCharge = tariff.cost();
        break;
      case PERCENT:
        advertiserCharge = tariff.percentOf(data.price());
        break;
      case DOUBLE_FIXED:
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

    BigDecimal affiliatePart = tariff.affiliatePart(advertiserCharge);
    BigDecimal heymoosePart = tariff.heymoosePart(advertiserCharge);
    OfferStat stat = copyStat(source, offer)
        .setProduct(data.product())
        .addToNotConfirmedRevenue(affiliatePart)
        .addToNotConfirmedFee(heymoosePart);
    if (tariff.cpaPolicy() == CpaPolicy.PERCENT) {
      stat.incSales();
    } else {
      stat.incLeads();
    }
    repo.put(stat);
    OfferAction action = new OfferAction(token, source.affiliate(), stat,
        source, offer, transactionId);
    repo.put(action);
    accounting.notConfirmedActionPayments(action, affiliatePart, heymoosePart);
    log.info("Tracked conversion for offer: '{} - {}'. " +
        "Affiliate money: '{}', heymoose fee: '{}'",
        new Object[] { offer.id(), offer.title(), affiliatePart, heymoosePart} );

    doPostBack(grant, action);
    data.setProcessed(true);
    return action;
  }
}

