package com.heymoose.resource;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.math.BigDecimal;

import static com.heymoose.resource.Exceptions.*;
import static java.lang.String.format;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

@Path("robokassa")
@Singleton
public class RobokassaResource {

  private final static Logger log = LoggerFactory.getLogger(RobokassaResource.class);
  
  private final String robokassaPass;
  private final Accounting accounting;
  private final Repo repo;

  @Inject
  public RobokassaResource(@Named("robokassaPass") String robokassaPass, Accounting accounting, Repo repo) {
    this.robokassaPass = robokassaPass;
    this.accounting = accounting;
    this.repo = repo;
  }

  @POST
  @Path("result")
  @Transactional
  @Produces("text/plain")
  public String result(@FormParam("OutSum") String _sum,
                     @FormParam("InvId") Long invId,
                     @FormParam("SignatureValue") String sig,
                     @FormParam("shpAccountId") Long accountId) {
    if (_sum == null) {
      logError(_sum, invId, sig, accountId, "OutSum is null");
      throw badRequest();
    }
    if (invId == null) {
      logError(_sum, invId, sig, accountId,"InvId is null");
      throw badRequest();
    }
    if (accountId == null) {
      logError(_sum, invId, sig, accountId,"shpAccountId is null");
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
    Account account = repo.get(Account.class, accountId);
    if (account == null) {
      logError(_sum, invId, sig, accountId, "account not found");
      throw notFound();
    }
    AccountingEntry add = new AccountingEntry(
        account,
        new BigDecimal(sum),
        AccountingEvent.ROBOKASSA_ADD,
        invId,
        "Robokassa " + DateTime.now().toString("dd.MM.YYYY HH:mm")
    );
    accounting.applyEntry(add);
    return "OK" + invId;
  }
  
  private static void logError(String sum, Long invId, String sig, Long accountId, String message) {
    log.error("Robokassa[OutSum: {}, InvId:{}, SignatureValue:{}, accountId:{}]: {}", new Object[]{sum, invId, sig, accountId, message});
  }

  private boolean validateSig(String sum, long invId, long accountId, String sig) {
    return md5Hex(format("%s:%d:%s:shpAccountId=%d", sum, invId, robokassaPass, accountId)).equalsIgnoreCase(sig);
  }
}
