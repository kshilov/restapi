package com.heymoose.resource;

import com.heymoose.domain.Account;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.badRequest;
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
import javax.ws.rs.Produces;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import org.joda.time.DateTime;
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
  @Produces("text/plain")
  public String result(@FormParam("OutSum") String _sum,
                     @FormParam("InvId") Long invId,
                     @FormParam("SignatureValue") String sig,
                     @FormParam("accountId") Long accountId) {
    if (_sum == null) {
      logError(_sum, invId, sig, accountId, "OutSum is null");
      throw badRequest();
    }
    if (invId == null) {
      logError(_sum, invId, sig, accountId,"InvId is null");
      throw badRequest();
    }
    if (accountId == null) {
      logError(_sum, invId, sig, accountId,"accountId is null");
      throw badRequest();
    }
    if (sig == null) {
      logError(_sum, invId, sig, accountId, "SignatureValue is null");
      throw badRequest();
    }
    if (!validateSig(_sum, invId, accountId, sig)) {
      logError(_sum, invId, sig, accountId, "bad sig");
      throw unauthorized();
    }
    double sum = Double.parseDouble(_sum);
    Account account = accounts.getAndLock(accountId);
    if (account == null) {
      logError(_sum, invId, sig, accountId, "account not found");
      throw notFound();
    }
    account.addToBalance(new BigDecimal(sum), "Robokassa " + DateTime.now().toString("dd.MM.YYYY HH:mm"));
    return "OK" + invId;
  }
  
  private static void logError(String sum, Long invId, String sig, Long accountId, String message) {
    log.error("Robokassa[OutSum: {}, InvId:{}, SignatureValue:{}, accountId:{}]: {}", new Object[]{sum, invId, sig, accountId, message});
  }

  private boolean validateSig(String sum, long invId, long accountId, String sig) {
    return md5Hex(format("%s:%d:%s:%d", sum, invId, robokassaPass, accountId)).equalsIgnoreCase(sig);
  }
}
