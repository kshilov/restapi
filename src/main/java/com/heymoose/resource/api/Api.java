package com.heymoose.resource.api;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Context;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.OfferShow;
import com.heymoose.domain.OfferShowRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.events.ActionApproved;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.conflict;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.resource.Exceptions.unauthorized;
import com.heymoose.resource.api.data.OfferData;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URIUtils;
import com.heymoose.util.URLEncodedUtils;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.hibernate.type.IntegerType;

@Singleton
public class Api {

  private final OfferRepository offers;
  private final PerformerRepository performers;
  private final AppRepository apps;
  private final ActionRepository actions;
  private final Accounts accounts;
  private final BigDecimal compensation;
//  private final EventBus eventBus;
  private final UserRepository users;
  private final OfferShowRepository offerShows;

  @Inject
  public Api(OfferRepository offers,
             PerformerRepository performers,
             AppRepository apps,
             ActionRepository actions,
             Accounts accounts,
             @Named("compensation") BigDecimal compensation,
//             EventBus eventBus,
             UserRepository users,
             OfferShowRepository offerShows) {
    this.offers = offers;
    this.performers = performers;
    this.apps = apps;
    this.actions = actions;
    this.accounts = accounts;
    this.compensation = compensation;
//    this.eventBus = eventBus;
    this.users = users;
    this.offerShows = offerShows;
  }

  @Transactional
  public Iterable<OfferData> getOffers(long appId, Integer hour, String extId, OfferRepository.Filter filter) {
    App app = apps.byId(appId);
    Performer performer = performers.byPlatformAndExtId(app.platform(), extId);
    if (performer == null) {
      performer = new Performer(extId, app.platform(), null);
      performers.put(performer);
    }
    return offers.availableFor(performer, filter, new Context(app, hour));
  }

//  @Transactional
//  public Iterable<Offer> logShows(Iterable<Offer> offers, App app, Performer performer) {
//    for (Offer offer : offers)
//      offerShows.put(new OfferShow(offer, app, performer));
//    return offers;
//  }

  public URI doOffer(long offerId, long appId, String extId) {
    OfferResult result = doOfferInternal(offerId, appId, extId);
//    if (result.approved != null)
//      eventBus.publish(result.approved);
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
  public OfferResult doOfferInternal(long offerId, long appId, String extId) {
    checkNotNull(offerId);
    if (isBlank(extId))
      throw conflict();
    App app = apps.byId(appId);
    Performer performer = performers.byPlatformAndExtId(app.platform(), extId);
    if (performer == null) {
      performer = new Performer(extId, app.platform(), null);
      performers.put(performer);
    }
    Offer offer = offers.byId(offerId);
    if (offer == null)
      throw notFound();

    if (!offer.reentrant()) {
      Action action = actions.byPerformerAndOfferAndApp(performer.id(), offer.id(), app.id());
      if (action != null) {
        if (action.done())
          return OfferResult.of(app.url());
        action.incAttempts();
        return OfferResult.of(URI.create(offer.url()));
      }
    }

    if (offer.order().disabled())
      throw conflict();
    accounts.lock(app.owner().developerAccount());
    Action action = new Action(accounts, offer, performer, app);
    if (offer.autoApprove())
      action.approve(accounts, compensation);
    actions.put(action);
    URI redirectUrl = appendQueryParam(URI.create(offer.url()), "action_id", action.id());
    redirectUrl = appendQueryParam(redirectUrl, "back_url", app.url());
    if (offer.autoApprove())
      return OfferResult.of(redirectUrl, new ActionApproved(action, compensation));
    else
      return OfferResult.of(redirectUrl);
  }
   
  public void approveAction(long customerId, long actionId) {
//    eventBus.publish(doApprove(customerId, actionId));
    doApprove(customerId, actionId);
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
    accounts.lock(action.app().owner().developerAccount());
    action.approve(accounts, compensation);
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

  @Transactional
  public void introducePerformer(Platform platform, String extId, Performer.Info info) {
    Performer performer = performers.byPlatformAndExtId(platform, extId);
    if (performer == null)
      performer = new Performer(extId, platform, null);
    performer.setInfo(info);
    performers.put(performer);
  }

  @Transactional
  public void reportShow(List<Long> offers, App app, String extId) {
   for (Offer offer : this.offers.byIds(offers).values())
     offerShows.put(new OfferShow(offer, app, performers.byPlatformAndExtId(app.platform(), extId)));
  }
}
