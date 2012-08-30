package com.heymoose.infrastructure.service.carolines;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CarolinesActionDataImporter
    implements ActionDataImporter<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(CarolinesActionDataImporter.class);

  private static final Predicate<OfferAction> NOT_APPROVED =
      new Predicate<OfferAction>() {
      @Override
      public boolean apply(OfferAction offerAction) {
        return offerAction.state() == OfferActionState.NOT_APPROVED;
      }
    };

  private final Repo repo;
  private final OfferActions actionService;

  @Inject
  public CarolinesActionDataImporter(Repo repo, OfferActions actionService) {
    this.repo = repo;
    this.actionService = actionService;
  }

  @Transactional
  @Override
  public void doImport(List<ItemListActionData> actionList,
                       Long parentOfferId) {
    for (ItemListActionData data : actionList) {
      doImport(data, parentOfferId);
    }
  }

  @Override
  public void doImport(ItemListActionData payment, Long parentOfferId) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token '{}' not found, skipping", payment.token());
      return;
    }

    // check whether token was not tracked already
    List<OfferAction> offerActionList = repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transaction_id = ?",
        token, payment.transactionId());

    if (payment.status() == ActionStatus.CANCELED) {
      for (OfferAction action : Iterables.filter(offerActionList, NOT_APPROVED)) {
        actionService.cancel(action);
      }
    }
  }
}
