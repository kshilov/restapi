package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfferActions {

  private final Repo repo;

  @Inject
  public OfferActions(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public void approve(OfferAction action) {
    checkArgument(action.state() == OfferActionState.NOT_APPROVED);
    action.approve();
    action.affiliateTx().approve();
    action.adminTx().approve();
  }

  @Transactional
  public void cancel(OfferAction action) {
    checkArgument(action.state() == OfferActionState.NOT_APPROVED);
    action.cancel();
    action.affiliateTx().cancel();
    action.adminTx().cancel();
  }
}
