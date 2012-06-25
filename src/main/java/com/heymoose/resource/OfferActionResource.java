package com.heymoose.resource;

import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.OfferActions;
import com.heymoose.domain.affiliate.OfferRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

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
  public void fix() {
    actions.fix();
  }
}
