package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.*;

public abstract class BaseActionProcessor
    implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(FixActionProcessor.class);

  private Repo repo;
  private Accounting accounting;
  private OfferGrantRepository offerGrants;

  protected BaseActionProcessor(Repo repo, Accounting accounting,
                                OfferGrantRepository offerGrants) {
    this.repo = repo;
    this.accounting = accounting;
    this.offerGrants = offerGrants;
  }


  public void process(ProcessableData data) {
    Token token = checkToken(repo, data.token());
    BaseOffer offer = data.offer();
    String transactionId = data.transactionId();
    OfferStat source = token.stat();
    OfferGrant grant = offerGrants.checkGrant(source.affiliate(), offer);

    OfferAction existed = checkIfActionExists(repo, offer, token, transactionId);

    BigDecimal advertiserCharge = advertiserCharge(data, existed);
    BigDecimal affiliatePart = offer.tariff().affiliatePart(advertiserCharge);
    BigDecimal heymoosePart = offer.tariff().heymoosePart(advertiserCharge);
    OfferStat stat = copyStat(source, offer)
        .addToNotConfirmedRevenue(affiliatePart)
        .addToNotConfirmedFee(heymoosePart);
    setCustomStatFields(data, stat);
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
  }

  protected abstract BigDecimal advertiserCharge(ProcessableData data,
                                                 OfferAction existedAction);
  protected abstract void setCustomStatFields(ProcessableData data,
                                              OfferStat stat);
}
