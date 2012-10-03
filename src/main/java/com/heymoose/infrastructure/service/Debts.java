package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.accounting.Withdrawal;
import com.heymoose.domain.accounting.WithdrawalPayment;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public final class Debts {

  public enum Ordering {
    OFFER_NAME("offer-name"), USER_EMAIL("user-email"),
    BASIS("basis"),
    PAYED_OUT("payed-out-amount"), DEBT("debt-amount"),
    INCOME("income-amount"), ORDERED("ordered-amount"),
    PENDING("pending-amount"), AVAILABLE("available-for-order-amount");

    public final String COLUMN;

    Ordering(String colName) { this.COLUMN = colName; }
  }

  private static Logger log = LoggerFactory.getLogger(Debts.class);

  private final Repo repo;
  private final Accounting accounting;

  @Inject
  public Debts(Repo repo, Accounting accounting) {
    this.repo = repo;
    this.accounting = accounting;
  }
  
  public Pair<QueryResult, Long> orderedWithdrawals(Long affId,
                                                    DataFilter<Ordering> filter) {
    return SqlLoader.templateQuery("ordered-withdrawals", repo.session())
        .addTemplateParam("grouped", true)
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .executeAndCount(filter.offset(), filter.limit());
  }
  
  public QueryResult sumOrderedWithdrawals(Long affId) {
    return SqlLoader.templateQuery("ordered-withdrawals", repo.session())
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .execute();
  }

  public Pair<QueryResult, Long> debtInfo(Long offerId,
                                          Long affId,
                                          DataFilter<Ordering> filter) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery("debt", repo.session())
        .addQueryParam("from", filter.from())
        .addQueryParam("to", filter.to())
        .addTemplateParam("grouped", true)
        .addTemplateParam("ordering", filter.ordering().COLUMN)
        .addTemplateParam("direction", filter.direction());

    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true);
      query.addQueryParam("offer_id", offerId);
    }
    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true);
      query.addQueryParam("aff_id", affId);
      query.addTemplateParam("forAffiliate", true);
    }
    return query.executeAndCount(filter.offset(), filter.limit());
  }

  public QueryResult sumDebt(Long affId, Long offerId,
                             DateTime from, DateTime to) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery("debt", repo.session())
        .addQueryParam("from", from.toDate())
        .addQueryParam("to", to.toDate());
    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true);
      query.addQueryParam("aff_id", affId);
    }
    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true);
      query.addQueryParam("offer_id", offerId);
    }
    return query.execute();
  }


  @SuppressWarnings("unchecked")
  public void payOffToAffiliate(Offer offer, Long userId,
                                Withdrawal.Basis basis,
                                BigDecimal available,
                                DateTime from, DateTime to) {
    Preconditions.checkNotNull(offer, "Offer can not be null.");
    log.info("Gonna make withdraws for offer: {} basis: {} " +
        "to user: {} amount: {} period: {} - {}.",
        new Object[] { offer.id(), basis, userId, available, from, to });
    Criteria criteria = repo.session().createCriteria(Withdrawal.class)
        .add(Restrictions.between("creationTime", from, to))
        .add(Restrictions.isNotNull("orderTime"))
        .add(Restrictions.eq("sourceId", offer.id()))
        .addOrder(Order.asc("creationTime"));
    if (userId != null) {
      criteria.add(Restrictions.eq("userId", userId));
    }
    if (basis != null) {
      criteria.add(Restrictions.eq("basis", basis));
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

      accounting.addOfferFunds(offer, toPayAvailable, payingFor.actionId());
      repo.put(new WithdrawalPayment()
          .setCreationTime(now)
          .setAmount(toPayAvailable)
          .setWithdrawalId(payingFor.id()));

      available = available.subtract(toPayAvailable);
    }
  }

  public void oweAffiliateRevenue(OfferAction action, BigDecimal amount) {

    repo.put(new Withdrawal()
        .setUserId(action.affiliate().id())
        .setSourceId(action.offer().master())
        .setActionId(action.id())
        .setAmount(amount)
        .setBasis(Withdrawal.Basis.AFFILIATE_REVENUE)
        .setCreationTime(action.creationTime()));
  }


  public void oweMlm(OfferAction action, BigDecimal mlmValue) {
    Preconditions.checkNotNull(action.affiliate().referrerId());
    repo.put(new Withdrawal()
        .setUserId(action.affiliate().referrerId())
        .setSourceId(action.offer().master())
        .setActionId(action.id())
        .setAmount(mlmValue)
        .setCreationTime(action.creationTime())
        .setBasis(Withdrawal.Basis.MLM));
  }


  public void oweFee(OfferAction action, BigDecimal amount) {
    repo.put(new Withdrawal()
        .setUserId(1L) // ?
        .setSourceId(action.offer().master())
        .setActionId(action.id())
        .setAmount(amount)
        .setBasis(Withdrawal.Basis.FEE)
        .setCreationTime(action.creationTime())
        .setOrderTime(DateTime.now())); // it's immediately ordered
  }



  @SuppressWarnings("unchecked")
  public void orderWithdrawal(Long affId) {
    User user = repo.get(User.class, affId);
    DateTime now = DateTime.now();
    List<Withdrawal> withdrawalList = (List<Withdrawal>)
        repo.session().createCriteria(Withdrawal.class)
        .add(Restrictions.isNull("orderTime"))
        .add(Restrictions.eq("userId", affId))
        .list();
    for (Withdrawal withdrawal : withdrawalList) {
      withdrawal.setOrderTime(now);
      repo.put(withdrawal);
      accounting.applyEntry(new AccountingEntry()
          .setAccount(user.affiliateAccount())
          .setAmount(withdrawal.amount().negate())
          .setSourceId(withdrawal.actionId())
          .setEvent(AccountingEvent.WITHDRAW));
    }
  }



}
