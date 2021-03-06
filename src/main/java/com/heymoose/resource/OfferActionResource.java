package com.heymoose.resource;

import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
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
import javax.ws.rs.core.Response;
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

  @DELETE
  @Path("by_id")
  @Transactional
  public Response cancelByIdList(@FormParam("offer_id") Long offerId,
                                 @FormParam("id") List<Long> idList) {
    checkNotNull(offerId);
    actions.cancelByIdList(existingOffer(offerId), idList);
    return Response.ok().build();
  }

  @PUT
  @Path("by_id")
  @Transactional
  public Response approveByIdList(@FormParam("offer_id") Long offerId,
                                  @FormParam("id") List<Long> idList) {
    checkNotNull(offerId);
    actions.approveByIdList(existingOffer(offerId), idList);
    return Response.ok().build();
  }

  @PUT
  @Path("verify")
  @Transactional
  public String approveByTransactions(@FormParam("offer_id") Long offerId,
                                   @FormParam("transactions")
                                   List<String> transactionIdList) {
    checkNotNull(offerId);
    return String.valueOf(actions.verify(existingOffer(offerId), transactionIdList,
        OfferActionState.APPROVED));
  }

  @DELETE
  @Path("verify")
  @Transactional
  public String cancelByTransactions(@FormParam("offer_id") Long offerId,
                                  @FormParam("transactions")
                                  List<String> transactionIdList) {
    checkNotNull(offerId);
    return String.valueOf(actions.verify(existingOffer(offerId), transactionIdList,
        OfferActionState.CANCELED));
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
      @QueryParam("date_kind") @DefaultValue("CREATION")
      OfferActions.DateKind dateKind,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("CREATION_TIME") OfferActions.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {
    checkNotNull(offerId);
    DataFilter<OfferActions.Ordering> filter =
        new DataFilter<OfferActions.Ordering>()
        .setFrom(new DateTime(from))
        .setTo(new DateTime(to))
        .setOrdering(ordering)
        .setDirection(direction)
        .setLimit(limit)
        .setOffset(offset);
    Pair<QueryResult, Long> result =
        actions.list(offerId, state, dateKind, filter);
    return new XmlOfferActions(result.fst, result.snd);

  }
}
