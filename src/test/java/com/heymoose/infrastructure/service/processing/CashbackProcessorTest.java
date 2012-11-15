package com.heymoose.infrastructure.service.processing;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.Assert.assertEquals;

public final class CashbackProcessorTest {

  private static AtomicLong IDS = new AtomicLong();

  @Test
  public void savesCashbackData() throws Exception {
    String cashbackTargetId = "client@domain.com";
    ProcessableData data = processableWithCashback(cashbackTargetId, null);

    Cashbacks cashbacks = mockCashbacks();
    new CashbackProcessor(cashbacks).process(data);

    List<Cashback> cashbackList = cashbacks.list();
    assertEquals(1, cashbackList.size());
    Cashback savedCashback = cashbackList.get(0);
    assertEquals(cashbackTargetId, savedCashback.targetId());
    assertEquals(data.offerAction(), savedCashback.action());
    assertEquals(data.offerAction().affiliate(), savedCashback.affiliate());
  }

  @Test
  public void doesNothingIfNoCahbackTargetId() throws Exception {
    OfferStat stat = new OfferStat();
    Token token = new Token(stat);
    ProcessableData data = new ProcessableData()
        .setToken(token);

    Cashbacks mockCashbacks = mockCashbacks();
    new CashbackProcessor(mockCashbacks).process(data);

    assertEquals(0, mockCashbacks.list().size());
  }

  @Test
  public void addsCashbackInvitationIfInvitationCookieIsSet() throws Exception {
    String cashbackTargetId = "cashback-referal@heymoose.com";
    String cashbackReferer = "cashback-referer@heymoose.com";
    ProcessableData data = processableWithCashback(
        cashbackTargetId, cashbackReferer);
    Cashbacks mockCashbacks = mockCashbacks();

    new CashbackProcessor(mockCashbacks).process(data);
    Cashback cashback = mockCashbacks.list().get(0);

    assertEquals(cashbackReferer, cashback.referer());
  }

  private ProcessableData processableWithCashback(String cashbackTarget,
                                                  String cashbackReferer) {
    OfferStat stat = new OfferStat()
        .setCashbackTargetId(cashbackTarget)
        .setCashbackReferer(cashbackReferer);
    Token token = new Token(stat);
    User user = new User();
    OfferAction action = new OfferAction()
        .setId(IDS.incrementAndGet())
        .setAffiliate(user);
    return new ProcessableData()
        .setOfferAction(action)
        .setToken(token);
  }

  private Cashbacks mockCashbacks() {
    return new Cashbacks() {
      ImmutableList.Builder<Cashback> list = ImmutableList.builder();

      @Override
      public List<Cashback> list() {
        return list.build();
      }

      @Override
      public Cashback add(Cashback cashback) {
        list.add(cashback);
        return cashback;
      }

      @Override
      public Pair<QueryResult, Long> list(Long affId, int offset, int limit) {
        return null;
      }

      @Override
      public boolean containTarget(String cashbackTarget) {
        for (Cashback cb : list.build()) {
          if (cb.targetId().equals(cashbackTarget)) return true;
        }
        return false;
      }

      @Override
      public Pair<QueryResult, Long> listInvites(Long affId, int offset, int limit) {
        return null;
      }
    };

  }
}
