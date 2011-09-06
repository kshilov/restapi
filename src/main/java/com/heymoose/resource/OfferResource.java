package com.heymoose.resource;

import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.security.Secured;
import com.heymoose.security.Signer;
import com.heymoose.util.jtpl.Template;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

@Path("offers")
@Secured
@Singleton
public class OfferResource {

  private final OfferRepository offers;
  private final Provider<Long> appIdProvider;
  private final AppRepository apps;

  @Inject
  public OfferResource(OfferRepository offers,
                       @Named("app") Provider<Long> appIdProvider,
                       AppRepository apps) {
    this.offers = offers;
    this.appIdProvider = appIdProvider;
    this.apps = apps;
  }

  private long appId() {
    return appIdProvider.get();
  }

  private String secret() {
    return apps.get(appId()).secret;
  }

  @GET
  @Produces("text/html; charset=utf-8")
  public Response get() throws IOException {
    StringBuilder html = new StringBuilder();
    StringWriter sw = new StringWriter();
    InputStream is = getClass().getResourceAsStream("/offer.jtpl");
    IOUtils.copy(is, sw);
    is.close();
    String offerTplSrc = sw.toString();
    for (Offer offer : offers.all()) {
      Template offerTpl = new Template(offerTplSrc);
      offerTpl.assign("TITLE", offer.title);
      offerTpl.assign("BODY", offer.body);
      offerTpl.assign("TIME", offer.creationTime.toGMTString());
      offerTpl.assign("APP", Long.toString(appId()));
      offerTpl.assign("SIG", Signer.sign(appId(), secret()));
      offerTpl.assign("OFFER", Long.toString(offer.id));
      offerTpl.parse("main");
      html.append(offerTpl.out());
    }
    return Response.ok(html.toString()).build();
  }
}
