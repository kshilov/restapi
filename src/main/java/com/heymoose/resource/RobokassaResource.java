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
                     @FormParam("InvId") Long accountId,
                     @FormParam("SignatureValue") String sig) {
    if (_sum == null) {
      logError(_sum, accountId, sig, "OutSum is null");
      throw badRequest();
    }
    if (accountId == null) {
      logError(_sum, accountId, sig, "InvId is null");
      throw badRequest();
    }
    if (sig == null) {
      logError(_sum, accountId, sig, "SignatureValue is null");
      throw badRequest();
    }
    if (!validateSig(_sum, accountId, sig)) {
      logError(_sum, accountId, sig, "bad sig");
      throw unauthorized();
    }
    double sum = Double.parseDouble(_sum);
    Account account = accounts.getAndLock(accountId);
    if (account == null) {
      logError(_sum, accountId, sig, "account not found");
      throw notFound();
    }
    account.addToBalance(new BigDecimal(sum), "Robokassa " + DateTime.now().toString("dd.MM.YYYY HH:mm"));
    return "OK" + accountId;
  }
  
  private static void logError(String sum, Long userId, String sig, String message) {
    log.error("Robokassa[OutSum: {}, InvId:{}, SignatureValue:{}]: {}", new Object[]{sum, userId, sig, message});
  }

  private boolean validateSig(String sum, long userId, String sig) {
    return md5Hex(format("%s:%d:%s", sum, userId, robokassaPass)).equalsIgnoreCase(sig);
  }
}
