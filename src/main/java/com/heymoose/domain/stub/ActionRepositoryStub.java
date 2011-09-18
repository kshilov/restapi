package com.heymoose.domain.stub;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.util.Paging;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Singleton
public class ActionRepositoryStub extends RepositoryStub<Action> implements ActionRepository {

  @Override
  public Action byId(long id) {
    Action action = super.byId(id);
    if (action.deleted)
      return null;
    return action;
  }

  @Override
  public Action byPerformerAndOffer(long performerId, long offerId) {
    for (Action action : all())
      if (action.performer.id.equals(performerId) && action.offer.id.equals(offerId))
        return action;
    return null;
  }

  @Override
  public Iterable<Action> list(final Ordering ordering, int offset, int limit) {
    Comparator<Action> actionComparator = new Comparator<Action>() {
      @Override
      public int compare(Action a1, Action a2) {
        switch (ordering) {
          case BY_CREATION_TIME_ASC:
            return a1.creationTime.compareTo(a2.creationTime);
          case BY_CREATION_TIME_DESC:
            return a2.creationTime.compareTo(a1.creationTime);
          default:
            throw new IllegalArgumentException("Unknown ordering: " + ordering);
        }
      }
    };
    List<Action> all = Lists.newArrayList(identityMap.values());
    Collections.sort(all, actionComparator);
    List<Action> page = Paging.page(all, offset, limit);
    return Collections.unmodifiableList(page);
  }

  @Override
  public Set<Action> all() {
    Set<Action> notDeleted = Sets.newHashSet();
    for (Action action : super.all())
      if (!action.deleted)
        notDeleted.add(action);
    return Collections.unmodifiableSet(notDeleted);
  }
}
