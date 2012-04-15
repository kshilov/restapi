package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@Singleton
public class Accounting {

  private final Repo repo;

  @Inject
  public Accounting(Repo repo) {
    this.repo = repo;
  }

  public void transferMoney(Account src, Account dst, BigDecimal amount, AccountingEvent event, Long sourceId) {
    transferMoney(src, dst, amount, event, sourceId, null);
  }

  public void applyEntry(AccountingEntry entry) {
    Session session = repo.session();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", entry.amount())
        .setParameter("id", entry.account().id())
        .executeUpdate();
    session.refresh(entry.account());
  }

  public void transferMoney(Account src, Account dst, BigDecimal amount, AccountingEvent event, Long sourceId, String descr) {
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

  public void transferMoney(Account src, AccountingEntry dstEntry, BigDecimal amount, AccountingEvent event, Long sourceId, String desc) {
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

  public void cancel(AccountingTransaction transaction) {
    List<AccountingEntry> entries = repo.allByHQL(
        AccountingEntry.class,
        "from AccountingEntry where transaction = ?",
        transaction
    );
    AccountingTransaction reverseTx = new AccountingTransaction();
    for (AccountingEntry entry : entries) {
      AccountingEntry reverseEntry = new AccountingEntry(entry.account(), entry.amount().negate());
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

  public AccountingEntry getLastEntry(Account account) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class);
    criteria.add(Restrictions.eq("account", account));
    criteria.addOrder(Order.desc("id"));
    return (AccountingEntry) repo.getExecutableCriteria(criteria).setMaxResults(1).uniqueResult();
  }

  public Withdraw withdraw(Account account, BigDecimal amount) {
    AccountingEntry entry = new AccountingEntry(account, amount.negate());
    applyEntry(entry);
    Withdraw withdraw = new Withdraw(account, amount);
    repo.put(withdraw);
    return withdraw;
  }

  public List<Withdraw> withdraws(Account account) {
    return repo.allByHQL(Withdraw.class, "from Withdraw where account = ? order by timestamp desc", account);
  }

  public Withdraw withdrawOfAccount(Account account, long withdrawId) {
    return repo.byHQL(Withdraw.class, "from Withdraw where account = ? and id = ?", account, withdrawId);
  }

  public void deleteWithdraw(Withdraw withdraw, String comment) {
    checkArgument(!isBlank(comment));
    AccountingEntry entry = new AccountingEntry(withdraw.account(), withdraw.amount());
    applyEntry(entry);
    repo.remove(withdraw);
  }

  public Account destination(AccountingTransaction transaction) {
    return repo.byHQL(
        Account.class,
        "select e.account from from AccountingEntry e where e.transaction = ? and e.amount > 0",
        transaction
    );
  }
}
