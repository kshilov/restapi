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
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.events.ActionApproved;
import com.heymoose.events.EventBus;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URIUtils;
import com.heymoose.util.URLEncodedUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.heymoose.resource.Exceptions.conflict;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.resource.Exceptions.unauthorized;
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
  private final UserRepository users;

  @Inject
  public Api(OfferRepository offers,
             PerformerRepository performers,
             AppRepository apps,
             ActionRepository actions,
             Accounts accounts,
             @Named("compensation") BigDecimal compensation,
             EventBus eventBus, UserRepository users) {
    this.offers = offers;
    this.performers = performers;
    this.apps = apps;
    this.actions = actions;
    this.accounts = accounts;
    this.compensation = compensation;
    this.eventBus = eventBus;
    this.users = users;
  }

  @Transactional
  public Iterable<Offer> getOffers(long appId, String extId) {
    Performer performer = performers.byAppAndExtId(appId, extId);
    if (performer == null)
      return offers.enabled();
    return offers.availableFor(performer.id());
  }

  public URI doOffer(long offerId, long appId, String extId, Platform platform) {
    OfferResult result = doOfferInternal(offerId, appId, extId, platform);
    if (result.approved != null)
      eventBus.publish(result.approved);
    return result.url;
  }

  private static class OfferResult {

    public final URI url;
    public final ActionApproved approved;

    public OfferResult(URI url, ActionApproved approved) {
      this.url = url;
      this.approved = approved;
    }

    public static OfferResult of(URI url) {
      return new OfferResult(url, null);
    }

    public static OfferResult of(URI url, ActionApproved approved) {
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
    Action action = actions.byPerformerAndOffer(performer.id(), offer.id());
    if (action != null) {
      action.incAttempts();
      return OfferResult.of(URI.create(offer.body()));
    }
    if (offer.order().disabled())
      throw conflict();
    accounts.lock(offer.order().account());
    action = new Action(offer, performer);
    actions.put(action);
    URI redirectUrl = appendQueryParam(URI.create(offer.body()), "action_id", action.id());
    redirectUrl = appendQueryParam(redirectUrl, "back_url", app.url());
    if (offer.autoApprove())
      return OfferResult.of(redirectUrl, new ActionApproved(action, compensation));
    else
      return OfferResult.of(redirectUrl);
  }
   
  public void approveAction(long customerId, long actionId) {
    eventBus.publish(doApprove(customerId, actionId));
  }

  @Transactional
  public ActionApproved doApprove(long customerId, long actionId) {
    User customer = users.byId(customerId);
    if (customer == null)
      throw notFound();
    Action action = actions.byId(actionId);
    if (action == null)
      throw notFound();
    if (!customer.id().equals(action.offer().order().customer().id()))
      throw unauthorized();
    if (action.done())
      return new ActionApproved(action, compensation);
    if (action.deleted())
      throw conflict();
    accounts.lock(action.performer().app().owner().developerAccount());
    action.approve(compensation);
    return new ActionApproved(action, compensation);
  }

  private static URI appendQueryParam(URI uri, String name, Object value) {
    List<NameValuePair> params = newArrayList(URLEncodedUtils.parse(uri, "UTF-8"));
    params.add(new NameValuePair(name, value.toString()));
    try {
      return URIUtils.createURI(
          uri.getScheme(),
          uri.getHost(),
          uri.getPort(),
          uri.getPath(),
          URLEncodedUtils.format(params, "UTF-8"),
          uri.getFragment()
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
