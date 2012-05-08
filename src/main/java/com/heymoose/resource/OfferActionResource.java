package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferActions;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("actions")
@Singleton
public class OfferActionResource {

  private final OfferActions actions;

  @Inject
  public OfferActionResource(OfferActions actions) {
    this.actions = actions;
  }

  @POST
  @Path("expired")
  public void approveAllExpired(@FormParam("offer_id") Long offerId, @FormParam("exclude_transaction") Set<String> excluded) {
    checkNotNull(offerId);
    actions.approveAllExpired(offerId, excluded);
  }
}
