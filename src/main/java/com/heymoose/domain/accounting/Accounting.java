package com.heymoose.domain.accounting;

import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface Accounting {

  enum DebtOrdering {
    OFFER_NAME("offer-name"), USER_EMAIL("user-email"),
    BASIS("basis"),
    PAYED_OUT("payed-out-amount"), DEBT("debt-amount"),
    INCOME("income-amount"), ORDERED("ordered-amount"),
    PENDING("pending-amount");

    public final String COLUMN;

    DebtOrdering(String colName) { this.COLUMN = colName; }
  }

  void transferMoney(Account src, Account dst, BigDecimal amount,
                     AccountingEvent event, Long sourceId);

  void applyEntry(AccountingEntry entry);

  void transferMoney(Account src, Account dst, BigDecimal amount,
                     AccountingEvent event, Long sourceId, String descr);

  void transferMoney(Account src, AccountingEntry dstEntry, BigDecimal amount,
                     AccountingEvent event, Long sourceId, String desc);

  void cancel(AccountingTransaction transaction);

  AccountingEntry getLastEntry(Account account);

  Withdraw withdraw(Account account, BigDecimal amount);
  
  void approveWithdraw(Withdraw withdraw);

  List<Withdraw> withdraws(Account account);

  Withdraw withdrawOfAccount(Account account, long withdrawId);

  void deleteWithdraw(Withdraw withdraw, String comment);

  Account destination(AccountingTransaction transaction);

  Pair<QueryResult, Long> debtGroupedByAffiliate(Long offerId,
                                                 DataFilter<DebtOrdering> filter);

  Map<String, Object> sumDebtForAffiliate(Long affId, DateTime from, DateTime to);

  Pair<QueryResult, Long> debtGroupedByOffer(Long affId,
                                             DataFilter<DebtOrdering> filter);

  Map<String, Object> sumDebtForOffer(Long offerId, DateTime from, DateTime to);

  void offerToAffiliate(Offer offer, Long userId, BigDecimal amount,
                        DateTime from, DateTime to);

  void addOfferFunds(Offer offer, BigDecimal amount);

}
