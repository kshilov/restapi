package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.processing.internal.MoneyDivider;
import com.heymoose.infrastructure.service.processing.internal.OfferStatProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.*;

public final class CustomActionProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(CustomActionProcessor.class);

  protected Repo repo;
  protected Accounting accounting;
  protected OfferGrantRepository offerGrants;

  private MoneyDivider money;
  private OfferStatProcessor offerStatProcessor;

  @Inject
  protected CustomActionProcessor(Repo repo, Accounting accounting,
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

    checkIfActionExists(repo, offer, token, transactionId);

    BigDecimal advertiserCharge = money.advertiserCharge();
    BigDecimal affiliatePart = money.affiliatePart();
    BigDecimal heymoosePart = money.heymoosePart();
    OfferStat stat = copyStat(source, offer)
        .addToNotConfirmedRevenue(affiliatePart)
        .addToNotConfirmedFee(heymoosePart);
    offerStatProcessor.process(stat);
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

  public void setMoneyDivider(MoneyDivider money) {
    this.money = money;
  }

  public void setOfferStatProcessor(OfferStatProcessor offerStatProcessor) {
    this.offerStatProcessor = offerStatProcessor;
  }
}

