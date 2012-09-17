package com.heymoose.infrastructure.service;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.accounting.Withdraw;
import com.heymoose.domain.accounting.Withdrawal;
import com.heymoose.domain.accounting.WithdrawalPayment;import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.heymoose.resource.Exceptions.conflict;
import static org.apache.commons.lang.StringUtils.isBlank;

@Singleton
public class AccountingHiber implements Accounting {

  private static final Logger log =
      LoggerFactory.getLogger(AccountingHiber.class);

  private final Repo repo;

  @Inject
  public AccountingHiber(Repo repo) {
    this.repo = repo;
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId) {
    transferMoney(src, dst, amount, event, sourceId, null);
  }

  @Override
  public void applyEntry(AccountingEntry entry) {
    Session session = repo.session();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", entry.amount())
        .setParameter("id", entry.account().id())
        .executeUpdate();
    session.refresh(entry.account());
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId, String descr) {
    checkArgument(amount.signum() == 1);
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, descr);
    AccountingEntry dstEntry = new AccountingEntry(dst, amount, event, sourceId, descr);
    createTransaction(srcEntry, dstEntry);
    Session session = repo.session();
    session.createQuery("update Account set balance = balance - :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", src.id())
        .executeUpdate();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry.account().id())
        .executeUpdate();
    session.refresh(dst);
    session.refresh(src);
  }

  @Override
  public void transferMoney(Account src, AccountingEntry dstEntry,
                            BigDecimal amount, AccountingEvent event,
                            Long sourceId, String desc) {
    checkArgument(amount.signum() == 1);
    Session session = repo.session();
    session.createQuery("update AccountingEntry set amount = amount + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry)
        .executeUpdate();
    session.refresh(dstEntry);
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, desc);
    srcEntry.setTransaction(dstEntry.transaction());
    repo.put(srcEntry);
    session.createQuery("update Account set balance = balance - :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", src.id())
        .executeUpdate();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry.account().id())
        .executeUpdate();
    session.refresh(src);
  }

  @Override
  public void cancel(AccountingTransaction transaction) {
    List<AccountingEntry> entries = repo.allByHQL(
        AccountingEntry.class,
        "from AccountingEntry where transaction = ?",
        transaction
    );
    AccountingTransaction reverseTx = new AccountingTransaction();
    for (AccountingEntry entry : entries) {
      AccountingEntry reverseEntry = new AccountingEntry(entry.account(), entry.amount().negate(), AccountingEvent.CANCELLED, entry.id(), null);
      reverseEntry.setTransaction(reverseTx);
      repo.put(reverseEntry);
      applyEntry(reverseEntry);
    }
  }

  private void createTransaction(AccountingEntry srcEntry, AccountingEntry dstEntry) {
    AccountingTransaction transaction = new AccountingTransaction();
    srcEntry.setTransaction(transaction);
    dstEntry.setTransaction(transaction);
    repo.put(srcEntry);
    repo.put(dstEntry);
  }

  @Override
  public AccountingEntry getLastEntry(Account account) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class);
    criteria.add(Restrictions.eq("account", account));
    criteria.addOrder(Order.desc("id"));
    return (AccountingEntry) repo.getExecutableCriteria(criteria).setMaxResults(1).uniqueResult();
  }

  @Override
  public Withdraw withdraw(Account account, BigDecimal amount) {
    if (amount.signum() < 1 || account.balance().compareTo(amount) < 0)
      throw conflict();
    Withdraw withdraw = new Withdraw(account, amount);
    repo.put(withdraw);
    AccountingEntry entry = new AccountingEntry(account, amount.negate(),
        AccountingEvent.WITHDRAW, withdraw.id(), null);
    repo.put(entry);
    applyEntry(entry);
    return withdraw;
  }
  
  @Override
  public void approveWithdraw(Withdraw withdraw) {
    withdraw.approve();
  };

  @Override
  public List<Withdraw> withdraws(Account account) {
    return repo.allByHQL(Withdraw.class,
        "from Withdraw where account = ? order by timestamp desc", account);
  }

  @Override
  public Withdraw withdrawOfAccount(Account account, long withdrawId) {
    return repo.byHQL(Withdraw.class, "from Withdraw where account = ? and id = ?", account, withdrawId);
  }

  @Override
  public void deleteWithdraw(Withdraw withdraw, String comment) {
    checkArgument(!isBlank(comment));
    AccountingEntry entry = new AccountingEntry(withdraw.account(), withdraw.amount());
    applyEntry(entry);
    repo.remove(withdraw);
  }

  @Override
  public Account destination(AccountingTransaction transaction) {
    return repo.byHQL(
        Account.class,
        "select e.account from AccountingEntry e where e.transaction = ? and e.amount > 0",
        transaction
    );
  }

  @Override
  public Pair<QueryResult, Long> debtGroupedByAffiliate(
      Long offerId, DataFilter<DebtOrdering> filter) {
    return SqlLoader.templateQuery("debt", repo.session())
        .addTemplateParam("groupByUser", true)
        .addTemplateParam("filterByOffer", true)
        .addTemplateParam("ordering", filter.ordering().COLUMN)
        .addTemplateParam("direction", filter.direction())
        .addQueryParam("offer_id", offerId)
        .addQueryParam("from", filter.from())
        .addQueryParam("to", filter.to())
        .executeAndCount(filter.offset(), filter.limit());
  }

  public Map<String, Object> sumDebtForAffiliate(Long affId, DateTime from, DateTime to) {
    return SqlLoader.templateQuery("debt", repo.session())
        .addTemplateParam("filterByAffiliate", true)
        .addQueryParam("aff_id", affId)
        .addQueryParam("from", from.toDate())
        .addQueryParam("to", to.toDate())
        .execute().get(0);
  }

  @Override
  public Pair<QueryResult, Long> debtGroupedByOffer(Long affId,
                                                    DataFilter<DebtOrdering> filter) {
    return SqlLoader.templateQuery("debt", repo.session())
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("filterByAffiliate", true)
        .addTemplateParam("ordering", filter.ordering().COLUMN)
        .addTemplateParam("direction", filter.direction())
        .addQueryParam("aff_id", affId)
        .addQueryParam("from", filter.from())
        .addQueryParam("to", filter.to())
        .executeAndCount(filter.offset(), filter.limit());
  }

  public Map<String, Object> sumDebtForOffer(Long offerId, DateTime from, DateTime to) {
    return SqlLoader.templateQuery("debt", repo.session())
        .addTemplateParam("filterByOffer", true)
        .addQueryParam("offer_id", offerId)
        .addQueryParam("from", from.toDate())
        .addQueryParam("to", to.toDate())
        .execute().get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void offerToAffiliate(Long offerId, Long userId, BigDecimal available,
                               DateTime from, DateTime to) {
    log.info("Gonna make withdraws for offer: {} to user: {} period: {} - {}",
        new Object[] { offerId, userId, from, to });
    Criteria criteria = repo.session().createCriteria(Withdrawal.class)
        .add(Restrictions.between("creationTime", from, to))
        .add(Restrictions.isNotNull("orderTime"))
        .addOrder(Order.asc("creationTime"));
    if (offerId != null) {
      criteria.add(Restrictions.eq("sourceId", offerId));
    }
    if (userId != null) {
      criteria.add(Restrictions.eq("userId", userId));
    }

    List<Withdrawal> matchedWithdrawalList = (List<Withdrawal>) criteria.list();
    log.info("Found {} matched withdrawals.", matchedWithdrawalList.size());
    int i = -1;
    DateTime now = DateTime.now();
    while (available.signum() > 0
        && ++i < matchedWithdrawalList.size()) {

      Withdrawal payingFor = matchedWithdrawalList.get(i);
      BigDecimal payedOut = (BigDecimal) repo.session()
          .createCriteria(WithdrawalPayment.class)
          .add(Restrictions.eq("withdrawalId", payingFor.id()))
          .setProjection(Projections.sum("amount"))
          .uniqueResult();
      payedOut = payedOut == null ? BigDecimal.ZERO : payedOut;
      BigDecimal toPay = payingFor.amount().subtract(payedOut);
      if (toPay.signum() == 0) {
        continue;
      } else if (toPay.signum() < 0) {
        log.warn("Withdrawal {} has more money paid out, than needed!",
            payingFor.id());
        continue;
      }
      BigDecimal diff = available.subtract(toPay);
      BigDecimal toPayAvailable = diff.signum() > -1 ? toPay : available;

      log.info("Paying for withdrawal: {}, amount: {}",
          payingFor.id(), toPayAvailable);

      repo.put(new WithdrawalPayment()
          .setCreationTime(now)
          .setAmount(toPayAvailable)
          .setWithdrawalId(payingFor.id()));

      available = available.subtract(toPayAvailable);
    }
  }
}
