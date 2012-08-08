package com.heymoose.resource;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.ListFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.resource.xml.XmlOfferActions;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Set;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Path("actions")
@Singleton
public class OfferActionResource {

  private final OfferActions actions;
  private final OfferRepository offers;

  @Inject
  public OfferActionResource(OfferActions actions, OfferRepository offers) {
    this.actions = actions;
    this.offers = offers;
  }

  @PUT
  @Transactional
  public String approveExpired(@FormParam("offer_id") Long offerId) {
    if (offerId == null)
      return actions.approveExpired(null).toString();

    return actions.approveExpired(existingOffer(offerId)).toString();
  }

  @DELETE
  @Transactional
  public String cancelByTransactions(@FormParam("offer_id") Long offerId,
                                     @FormParam("transactions") Set<String> transactionIds) {
    checkNotNull(offerId);
    return actions.cancelByTransactions(existingOffer(offerId), transactionIds).toString();
  }

  private Offer existingOffer(long id) {
    Offer offer = offers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }

  @GET
  @Path("fix")
  public String fix() {
    actions.fix();
    return "OK";
  }


  @GET
  @Transactional
  public XmlOfferActions actionList(
      @QueryParam("offer_id") Long offerId,
      @QueryParam("state") OfferActionState state,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("CREATION_TIME") OfferActions.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {
    checkNotNull(offerId);
    ListFilter filter = new ListFilter()
        .setFrom(new DateTime(from))
        .setTo(new DateTime(to))
        .setLimit(limit)
        .setOffset(offset);
    List<OfferAction> result =
        actions.list(offerId, state, filter, ordering, direction);
    Long count = actions.count(offerId, state);
    return new XmlOfferActions(result, count);

  }
}
