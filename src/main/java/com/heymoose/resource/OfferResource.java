package com.heymoose.resource;

import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;

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

  @Inject
  public OfferResource(OfferRepository offers,
                       @Named("app") Provider<Long> appIdProvider,
                       AppRepository apps,
                       PerformerRepository performers,
                       ActionRepository actions) throws IOException {
    this.offers = offers;
    this.appIdProvider = appIdProvider;
    this.apps = apps;
    this.performers = performers;
    this.actions = actions;

    StringWriter sw = new StringWriter();
    InputStream is = getClass().getResourceAsStream("/offer.jtpl");
    IOUtils.copy(is, sw);
    is.close();
    this.offerTpl = sw.toString();
  }

  private long appId() {
    return appIdProvider.get();
  }

  private String secret() {
    return apps.byId(appId()).secret();
  }

  private String applyTemplate(String extId, Offer offer) {
    Template offerTpl = new Template(this.offerTpl);
    offerTpl.assign("TITLE", offer.title());
    offerTpl.assign("BODY", offer.body());
     offerTpl.assign("TIME", offer.creationTime().toString());
    offerTpl.assign("APP", Long.toString(appId()));
    offerTpl.assign("SIG", Signer.sign(appId(), secret()));
    offerTpl.assign("OFFER", Long.toString(offer.id()));
    if (!isBlank(extId))
      offerTpl.assign("EXT", extId);
    else
      offerTpl.assign("EXT", "");
    offerTpl.parse("main");
    return offerTpl.out();
  }

  @GET
  @Path("all")
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response all() {
    StringBuilder html = new StringBuilder();
    for (Offer offer : offers.approved())
      html.append(applyTemplate("", offer));
    return Response.ok(html.toString()).build();
  }


  @GET
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response get(@QueryParam("extId") String extId) {
    checkNotNull(extId);
    StringBuilder html = new StringBuilder();
    for (Offer offer : getAvailableOffers(extId))
      html.append(applyTemplate(extId, offer));
    return Response.ok(html.toString()).build();
  }

  @GET
  @Path("done")
  @Produces("text/html; charset=utf-8")
  @Transactional
  public Response getDone(@QueryParam("extId") String extId) {
    checkNotNull(extId);
    StringBuilder html = new StringBuilder();
    for (Offer offer : getDoneOffers(extId))
      html.append(applyTemplate(extId, offer));
    return Response.ok(html.toString()).build();
  }

  @GET
  @Path("internal/available")
  @Transactional
  public Response availableOffers(@QueryParam("extId") String extId) {
    return Response.ok(Mappers.toXmlOffers(getAvailableOffers(extId))).build();
  }

  @Transactional
  public Iterable<Offer> getAvailableOffers(String extId) {
    if (apps.byId(appId()).deleted())
      return Collections.emptySet();
    Performer performer = performers.byAppAndExtId(appId(), extId);
    if (performer == null)
      return offers.approved();
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
  @Transactional
  public Response doOffer(@PathParam("id") Long offerId,
                          @FormParam("extId") String extId,
                          @FormParam("platform") Platform platform) {
    checkNotNull(offerId, platform);
    if (isBlank(extId))
      return Response.status(400).build();
    App app = apps.byId(appId());
    app.assignPlatform(platform);
    Performer performer = performers.byAppAndExtId(appId(), extId);
    if (performer == null) {
      performer = new Performer(extId, app, null);
      performers.put(performer);
    }
    Offer offer = offers.byId(offerId);
    if (offer == null)
      return Response.status(404).build();
    if (!offer.order().approved())
      return Response.status(409).build();
    Action action = actions.byPerformerAndOffer(performer.id(), offer.id());
    if (action != null)
      return Response.status(Response.Status.CONFLICT).build();
    action = new Action(offer, performer);
    actions.put(action);
    return Response.status(302).location(URI.create(offer.body())).build();
  }
}
