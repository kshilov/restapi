package com.heymoose.resource;

import com.heymoose.resource.api.data.OfferData;

public interface OfferTemplate {
  public String render(Iterable<OfferData> offers);
}
