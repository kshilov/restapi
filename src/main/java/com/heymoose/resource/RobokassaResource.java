package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.resource.Exceptions.unauthorized;
import static java.lang.String.format;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("robokassa")
@Singleton
public class RobokassaResource {

  private final static Logger log = LoggerFactory.getLogger(RobokassaResource.class);
  
  private final String robokassaPass;
  private final UserRepository users;
  private final Accounts accounts;

  @Inject
  public RobokassaResource(@Named("robokassaPass") String robokassaPass, UserRepository users, Accounts accounts) {
    this.robokassaPass = robokassaPass;
    this.users = users;
    this.accounts = accounts;
  }

  @POST
  @Path("result")
  @Transactional
  public void result(@FormParam("nOutSum") double sum,
                     @FormParam("nInvId") long userId,
                     @FormParam("sSignatureValue") String sig) {
    if (!validateSig(sum, userId, sig)) {
      log.error("Robokassa[nOutSum: {}, nInvId:{}, sSignatureValue:]: bad sig", new Object[]{sum, userId, sig});
      throw unauthorized();
    }
    User user = users.byId(userId);
    if (user == null) {
      log.error("Robokassa[nOutSum: {}, nInvId:{}, sSignatureValue:]: user not found", new Object[]{sum, userId, sig});
      throw notFound();
    }
    if (!user.isCustomer()) {
      log.error("Robokassa[nOutSum: {}, nInvId:{}, sSignatureValue:]: not a customer", new Object[]{sum, userId, sig});
      throw unauthorized();
    }
    accounts.lock(user.customerAccount());
    user.customerAccount().addToBalance(new BigDecimal(sum), "Robokassa");
  }

  private boolean validateSig(double sum, long userId, String sig) {
    return md5Hex(format("%s:%d:%s", Double.toString(sum), userId, robokassaPass)).equals(sig);
  }
}
