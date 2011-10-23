package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.Offer;

import java.math.BigDecimal;

public interface OfferTemplate {
  public String render(Iterable<Offer> offers, App app, String extId, BigDecimal compensation);
}
