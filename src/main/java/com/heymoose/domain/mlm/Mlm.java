package com.heymoose.domain.mlm;

import com.heymoose.domain.User;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Mlm {

  private final Logger log = LoggerFactory.getLogger(Mlm.class);

  private final Repo repo;
  private final Accounting accounting;
  private final double mlmRatio;

  @Inject
  public Mlm(Repo repo, Accounting accounting, @Named("mlm-ratio") double mlmRatio) {
    this.repo = repo;
    this.accounting = accounting;
    this.mlmRatio = mlmRatio;
  }

  @Transactional
  public void doExport() {
    log.info("doExport");

    MlmExecution lastExecution = (MlmExecution) repo.session()
        .createQuery("from MlmExecution order by creationTime desc")
        .setMaxResults(1)
        .uniqueResult();

    MlmExecution currentExecution = new MlmExecution();
    repo.put(currentExecution);

    Date lastExecutionDate = (lastExecution == null)
        ? new Date(0)
        : lastExecution.creationTime().toDate();
    Date currentExecutionDate = currentExecution.creationTime().toDate();

    log.info("last execution date: {}", lastExecutionDate);

    String sql = "select u.id, sum(e.amount) from user_profile u " +
        "join accounting_entry e on u.affiliate_account_id = e.account_id " +
        "where e.event = 2 and u.referrer is not null " +
        "and e.creation_time > :last and e.creation_time <= :current group by u.id";

    List<Object[]> dbResult = repo.session().createSQLQuery(sql)
        .setParameter("last", lastExecutionDate)
        .setParameter("current", currentExecutionDate)
        .list();

    log.info("exporting {} entries", dbResult.size());

    for (Object[] record : dbResult) {
      long userId = ((BigInteger) record[0]).longValue();
      BigDecimal revenue = (BigDecimal) record[1];
      revenue = revenue.multiply(new BigDecimal(mlmRatio));
      User user = repo.get(User.class, userId);
      User referrer = repo.get(User.class, user.referrerId());
      log.info("transferring {} from user[{}] to user[{}]", new Object[]{revenue.doubleValue(), user.affiliateAccount().id(), referrer.affiliateAccount().id()});
      accounting.transferMoney(user.affiliateAccount(), referrer.affiliateAccount(), revenue, AccountingEvent.MLM, userId);
    }
  }
}
