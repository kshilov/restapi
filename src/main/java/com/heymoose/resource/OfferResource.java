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
import com.heymoose.resource.xml.Mappers;
import com.heymoose.security.Secured;
import com.heymoose.security.Signer;
import com.heymoose.util.jtpl.Template;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;

import static com.heymoose.domain.Compensation.subtractCompensation;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;

@Path("offers")
@Secured
@Singleton
public class OfferResource {

  private final OfferRepository offers;
  private final Provider<Long> appIdProvider;
  private final AppRepository apps;
  private final String offerTpl;
  private final PerformerRepository performers;
  private final ActionRepository actions;
  private final BigDecimal compensation;
  private final EventBus eventBus;
  private final Accounts accounts;

  @Inject
  public OfferResource(OfferRepository offers,
                       @Named("app") Provider<Long> appIdProvider,
                       AppRepository apps,
                       PerformerRepository performers,
                       ActionRepository actions,
                       @Named("compensation") BigDecimal compensation,
                       EventBus eventBus,
                       Accounts accounts) throws IOException {
    this.offers = offers;
    this.appIdProvider = appIdProvider;
    this.apps = apps;
    this.performers = performers;
    this.actions = actions;
    this.compensation = compensation;
    this.eventBus = eventBus;
    this.accounts = accounts;

    StringWriter sw = new StringWriter();
    InputStream is = getClass().getResourceAsStream("/offer.jtpl");
    IOUtils.copy(is, sw);
    is.close();
    this.offerTpl = sw.toString();
  }

  private long appId() {
    return appIdProvider.get();
  }

  private App app() {
    return apps.byId(appId());
  }

  private void applyTemplate(Template offerTpl, String extId, Offer offer, App app) {
    offerTpl.assign("TITLE", offer.title());
    offerTpl.assign("DESCRIPTION", offer.description());
    offerTpl.assign("BODY", offer.body());
    offerTpl.assign("IMG", offer.imageBase64());
    offerTpl.assign("APP", Long.toString(app.id()));
    offerTpl.assign("SIG", Signer.sign(app.id(), app.secret()));
    offerTpl.assign("OFFER", Long.toString(offer.id()));
    offerTpl.assign("PAYMENT", subtractCompensation(offer.order().cpa(), compensation).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
    if (!isBlank(extId))
      offerTpl.assign("EXT", extId);
    else
      offerTpl.assign("EXT", "");
    offerTpl.parse("main.offer");
  }

  @GET
  @Path("all")
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response all() {
    Template out = new Template(offerTpl);
    App app = app();
    for (Offer offer : offers.enabled())
      applyTemplate(out, "", offer, app);
    out.parse("main");
    return Response.ok(out.out()).build();
  }

  @GET
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response get(@QueryParam("uid") String extId) {
    checkNotNull(extId);
    Template out = new Template(offerTpl);
    App app = app();
    for (Offer offer : getAvailableOffers(extId))
      applyTemplate(out, extId, offer, app);
    out.parse("main");
    return Response.ok(out.out()).build();
  }

  @GET
  @Path("done")
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response getDone(@QueryParam("extId") String extId) {
    checkNotNull(extId);
    Template out = new Template(offerTpl);
    App app = app();
    for (Offer offer : getDoneOffers(extId))
      applyTemplate(out, extId, offer, app);
    out.parse("main");
    return Response.ok(out.out()).build();
  }

  @GET
  @Path("internal/available")
  @Transactional
  public Response availableOffers(@QueryParam("extId") String extId) {
    return Response.ok(Mappers.toXmlOffers(getAvailableOffers(extId))).build();
  }

  @Transactional
  public Iterable<Offer> getAvailableOffers(String extId) {
    Performer performer = performers.byAppAndExtId(appId(), extId);
    if (performer == null)
      return offers.enabled();
    return offers.availableFor(performer.id());
  }

  private Iterable<Offer> getDoneOffers(String extId) {
    Performer performer = performers.byAppAndExtId(appId(), extId);
    if (performer == null)
      return Collections.emptyList();
    return offers.doneFor(performer.id());
  }

  @POST
  @Path("{id}")
  public Response doOffer(@PathParam("id") Long offerId,
                          @FormParam("extId") String extId,
                          @FormParam("platform") Platform platform) {
    OfferResult result = doOfferInternal(offerId, extId, platform);
    if (result.approved != null)
      eventBus.publish(result.approved);
    return Response.status(302).location(URI.create(result.url)).build();
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
  public OfferResult doOfferInternal(Long offerId, String extId, Platform platform) {
    checkNotNull(offerId, platform);
    if (isBlank(extId))
      throw new WebApplicationException(409);
    App app = apps.byId(appId());
    app.assignPlatform(platform);
    Performer performer = performers.byAppAndExtId(appId(), extId);
    if (performer == null) {
      performer = new Performer(extId, app, null);
      performers.put(performer);
    }
    Offer offer = offers.byId(offerId);
    if (offer == null)
      throw new WebApplicationException(404);
    if (offer.order().disabled())
      throw new WebApplicationException(409);
    Action action = actions.byPerformerAndOffer(performer.id(), offer.id());
    if (action != null)
      throw new WebApplicationException(409);
    accounts.lock(offer.order().account());
    action = new Action(offer, performer);
    actions.put(action);
    if (offer.autoApprove())
      return OfferResult.of(offer.body(), new ActionApproved(action, compensation));
    else
      return OfferResult.of(offer.body());
  }
}
