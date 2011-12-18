package com.heymoose.domain;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.base.Repository;
import static com.heymoose.resource.Exceptions.badRequest;
import java.util.Set;

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

    public static class BannerEntry extends Entry {
      
      public final int width;
      public final int height;

      public BannerEntry(Offer.Type type, int count, int width, int height) {
        super(type, count);
        this.width = width;
        this.height = height;
      }
    }

    public final Iterable<Entry> entries;

    public Filter(String str) {
      ImmutableList.Builder<Entry> entryBuilder = ImmutableList.builder();
      for (String entry : str.split(",")) {
        String[] parts = entry.split(":");
        if (parts.length < 2)
          throw badRequest();
        Offer.Type type = Offer.Type.fromOrdinal(Integer.valueOf(parts[0]));
        int count = Integer.valueOf(parts[1]);
        if (type == Offer.Type.BANNER) {
          int width;
          int height;
          try {
            String size = parts[2];
            String[] sizeParts = size.split("x");
            width = Integer.valueOf(sizeParts[0]);
            height = Integer.valueOf(sizeParts[1]);
          } catch (Exception e) {
            throw badRequest();
          }
          entryBuilder.add(new BannerEntry(type, count, width, height));
        } else {
          entryBuilder.add(new Entry(type, count));
        }
      }
      entries = entryBuilder.build();
    }
  }

  Set<Offer> availableFor(Performer performer, Filter filter);
  Set<Offer> doneFor(long performerId);
}
