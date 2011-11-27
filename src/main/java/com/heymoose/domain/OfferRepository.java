package com.heymoose.domain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.base.Repository;

import static com.heymoose.resource.Exceptions.badRequest;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import sun.net.idn.StringPrep;

public interface OfferRepository extends Repository<Offer> {

  public static class Filter {

    public static class Entry {

      public final Offer.Type type;
      public final int count;

      public Entry(Offer.Type type, int count) {
        this.type = type;
        this.count = count;
      }
    }

    public final Iterable<Entry> entries;

    public Filter(String str) {
      ImmutableList.Builder<Entry> entryBuilder = ImmutableList.builder();
      for (String entry : str.split(",")) {
        String[] pair = entry.split(":");
        if (pair.length < 2)
          throw badRequest();
        Offer.Type type = Offer.Type.fromOrdinal(Integer.valueOf(pair[0]));
        int count = Integer.valueOf(pair[1]);
        entryBuilder.add(new Entry(type, count));
      }
      entries = entryBuilder.build();
    }
  }

  Set<Offer> availableFor(Performer performer, Filter filter);
  Set<Offer> doneFor(long performerId);
}
