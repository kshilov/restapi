package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
import com.heymoose.events.ActionApproved;
import com.heymoose.events.EventBus;
import com.heymoose.hibernate.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.net.URI;

import static com.heymoose.resource.Exceptions.conflict;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;

@Singleton
public class Api {

  private final OfferRepository offers;
  private final PerformerRepository performers;
  private final AppRepository apps;
  private final ActionRepository actions;
  private final Accounts accounts;
  private final BigDecimal compensation;
  private final EventBus eventBus;

  @Inject
  public Api(OfferRepository offers,
             PerformerRepository performers,
             AppRepository apps,
             ActionRepository actions,
             Accounts accounts,
             @Named("compensation") BigDecimal compensation,
             EventBus eventBus) {
    this.offers = offers;
    this.performers = performers;
    this.apps = apps;
    this.actions = actions;
    this.accounts = accounts;
    this.compensation = compensation;
    this.eventBus = eventBus;
  }

  @Transactional
  public Iterable<Offer> getOffers(long appId, String extId) {
    Performer performer = performers.byAppAndExtId(appId, extId);
    if (performer == null)
      return offers.approved();
    return offers.availableFor(performer.id());
  }

  public URI doOffer(long offerId, long appId, String extId, Platform platform) {
    OfferResult result = doOfferInternal(offerId, appId, extId, platform);
    if (result.approved != null)
      eventBus.publish(result.approved);
    return URI.create(result.url);
  }

  private static class OfferResult {

    public final String url;
    public final ActionApproved approved;

    public OfferResult(String url, ActionApproved approved) {
      this.url = url;
      this.approved = approved;
    }

    public static OfferResult of(String url) {
      return new OfferResult(url, null);
    }

    public static OfferResult of(String url, ActionApproved approved) {
      return new OfferResult(url, approved);
    }
  }

  @Transactional
  public OfferResult doOfferInternal(long offerId, long appId, String extId, Platform platform) {
    checkNotNull(offerId, platform);
    if (isBlank(extId))
      throw conflict();
    App app = apps.byId(appId);
    app.assignPlatform(platform);
    Performer performer = performers.byAppAndExtId(appId, extId);
    if (performer == null) {
      performer = new Performer(extId, app, null);
      performers.put(performer);
    }
    Offer offer = offers.byId(offerId);
    if (offer == null)
      throw notFound();
    if (!offer.order().approved())
      throw conflict();
    Action action = actions.byPerformerAndOffer(performer.id(), offer.id());
    if (action != null)
      throw conflict();
    accounts.lock(offer.order().account());
    action = new Action(offer, performer);
    actions.put(action);
    if (offer.autoApprove())
      return OfferResult.of(offer.body(), new ActionApproved(action, compensation));
    else
      return OfferResult.of(offer.body());
  }
}
