package com.heymoose.domain.stub;

import com.google.common.collect.Sets;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;

@Singleton
public class OfferRepositoryStub extends RepositoryStub<Offer> implements OfferRepository {

  private final ActionRepository actions;

  @Inject
  public OfferRepositoryStub(ActionRepository actions) {
    this.actions = actions;
  }

  @Override
  public Set<Offer> availableFor(long performerId) {
    Set<Action> already = Sets.newHashSet();
    for (Action action : actions.all())
      if (action.performer.id.equals(performerId)) {
        already.add(action);
      }
    Set<Offer> offers = Sets.newHashSet();
    for (Offer offer : all())
      if (offer.order.cpa.compareTo(offer.order.account.currentState().balance()) != 1)
        offers.add(offer);
    for (Action action : already)
      offers.remove(action.offer);
    return Collections.unmodifiableSet(offers);
  }

  @Override
  public Set<Offer> doneFor(long performerId) {
    Set<Offer> done = Sets.newHashSet();
    for (Action action : actions.all())
      if (action.performer.id.equals(performerId) && action.done)
        done.add(action.offer);
    return Collections.unmodifiableSet(done);
  }

  @Override
  public Set<Offer> approved() {
    Set<Offer> approved = Sets.newHashSet();
    for (Offer offer : all())
      if (offer.order.approved)
        approved.add(offer);
    return Collections.unmodifiableSet(approved);
  }

  @Override
  public Offer byId(long id) {
    Offer offer = super.byId(id);
    if (offer.order.deleted)
      return null;
    return offer;
  }

  @Override
  public Set<Offer> all() {
    Set<Offer> offers = Sets.newHashSet();
    for (Offer offer : super.all())
      if (!offer.order.deleted)
        offers.add(offer);
    return Collections.unmodifiableSet(offers);
  }
}
