package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferActions;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("actions")
@Singleton
public class OfferActionResource {

  private final OfferActions actions;

  @Inject
  public OfferActionResource(OfferActions actions) {
    this.actions = actions;
  }

  @PUT
  public String approveExpired(@FormParam("offer_id") Long offerId) {
    checkNotNull(offerId);
    return actions.approveExpired(offerId).toString();
  }
  
  @DELETE
  public String cancelByTransactions(@FormParam("offer_id") Long offerId,
                                   @FormParam("transcations") Set<String> transactionIds) {
    checkNotNull(offerId);
    return actions.cancelByTransactions(offerId, transactionIds).toString();
  }
}
