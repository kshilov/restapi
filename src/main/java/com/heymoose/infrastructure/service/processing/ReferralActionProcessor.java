package com.heymoose.infrastructure.service.processing;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.settings.Settings;
import com.heymoose.domain.user.User;
import com.heymoose.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferralActionProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(ReferralActionProcessor.class);

  private final UserRepository users;
  private final Settings settings;

  @Inject
  public ReferralActionProcessor(UserRepository users,
                                 Settings settings) {
    this.users = users;
    this.settings = settings;
  }

  public void process(ProcessableData data) {
    Long referralOfferId = settings.getLongOrNull(Settings.REFERRAL_OFFER);
    if (referralOfferId == null) return;
    if (!data.offer().id().equals(referralOfferId)) return;

    log.info("Entering referral processing. {}", data);
    Preconditions.checkNotNull(data.offerAction(),
        "Action not filled. " + data);
    OfferAction action = data.offerAction();
    Preconditions.checkNotNull(action.transactionId(),
        "Transaction id should contain id of referral.");
    User referral = users.byEmail(action.transactionId().trim());
    Preconditions.checkState(referral != null,
        "Referral with id " + action.transactionId() + " not found!");

    log.info("Setting referrer for user: {}, referrer: {}",
        referral, action.affiliate().id());
    users.put(referral.setReferrerId(action.affiliate().id()));

  }
}
