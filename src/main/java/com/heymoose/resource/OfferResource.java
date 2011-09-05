package com.heymoose.resource;

import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.security.Secured;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
  public Response get() {
    StringBuilder html = new StringBuilder();
    for (Offer offer : offers.all()) {
      html.append("\t<div style=\"width: 607px; height: 136px; margin: auto; border: 1px solid black; color: black; background-color: white; font-family: Arial; margin-bottom: 4px;\">\n" +
          "\t\t<div style=\"width: 128px; height: 128px; float: left; border: 1px solid black; margin: 3px;\">\n" +
          "\t\t</div>\n" +
          "\t\t<div style=\"width: 461px; float: left; margin: 4px;\">\n" +
          "\t\t\t<div style=\"width: 100%; height: 32px; font: bold 18px/32px Arial; margin: 0;\">\n" +
          "\t\t\t\t" + offer.action.title + "\n" +
          "\t\t\t</div>\n" +
          "\t\t\t<div style=\"height: 60px; width: 100%; font: normal 14px/20px Arial; text-align: justify; margin: 0;\">\n" +
          "\t\t\t\t" + offer.action.body + "\n" +
          "\t\t\t</div>\n" +
          "\t\t\t<div style=\"margin: 0;\">\n" +
          "\t\t\t\t<div style=\"float: left; border: 1px solid black; color: black; height:20px; background-color: #F5F5DC; padding: 4px; margin-top: 7px;\">\n" +
          "\t\t\t\t" + offer.action.creationTime.toGMTString() + "\n" +
          "\t\t\t\t</div>\n" +
          "\t\t\t\t<div style=\"float: right; color: orange; height:20px; padding: 4px; margin-top: 7px;\">\n" +
          "\t\t<form id=\"offerform\" action=\"/offers/go?app=" + appId() + "&secret=" + secret() + "\" method=\"post\" target=\"_blank\">\n" +
          "\t\t<input type=\"hidden\" name=\"offer_id\" value=\"" + offer.id + "\">\n" +
          "\t\t<input type=\"hidden\" name=\"error_url\" value=\"{{ params.error_url }}\">\n" +
          "\t\t<input type=\"hidden\" name=\"user_id\" class=\"user_id\" value=\"\">\n" +
          "\t\t<input type=\"submit\" class=\"send\" value=\"Пройти оффер\" />\n" +
          "\t\t</form>\n" +
          "\t\t\t\t</div>\n" +
          "\t\t\t</div>\n" +
          "\t\t</div>\n" +
          "\t</div>");
    }
    return Response.ok(html.toString()).build();
  }
}
