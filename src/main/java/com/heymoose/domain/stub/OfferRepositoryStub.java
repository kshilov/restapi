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
      if (action.performer.id.equals(performerId))
        already.add(action);
    Set<Offer> offers = Sets.newHashSet(all());
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
    return done;
  }
}
