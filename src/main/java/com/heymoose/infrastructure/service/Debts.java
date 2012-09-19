package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.Withdrawal;
import com.heymoose.domain.accounting.WithdrawalPayment;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
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


  public Pair<QueryResult, Long> groupedByAffiliate(
      Long offerId, DataFilter<Ordering> filter) {
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

  public Pair<QueryResult, Long> groupedByOffer(Long affId,
                                                DataFilter<Ordering> filter) {
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
  public void payOffToAffiliate(Offer offer, Long userId, BigDecimal available,
                               DateTime from, DateTime to) {
    Preconditions.checkArgument(offer != null, "Offer can not be null.");
    log.info("Gonna make withdraws for offer: {} to user: {} period: {} - {}.",
        new Object[] { offer.id(), userId, from, to });
    Criteria criteria = repo.session().createCriteria(Withdrawal.class)
        .add(Restrictions.between("creationTime", from, to))
        .add(Restrictions.isNotNull("orderTime"))
        .add(Restrictions.eq("sourceId", offer.id()))
        .addOrder(Order.asc("creationTime"));
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

      accounting.addOfferFunds(offer, toPayAvailable);
      repo.put(new WithdrawalPayment()
          .setCreationTime(now)
          .setAmount(toPayAvailable)
          .setWithdrawalId(payingFor.id()));

      available = available.subtract(toPayAvailable);
    }
  }


  public void orderWithdrawal(Long affId) {
    repo.session().createQuery(
        "update Withdrawal " +
        "set orderTime = ? " +
        "where orderTime = null")
        .setDate(0, DateTime.now().toDate())
        .executeUpdate();
  }



}
