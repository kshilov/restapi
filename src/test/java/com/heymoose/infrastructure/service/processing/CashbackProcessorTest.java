package com.heymoose.infrastructure.service.processing;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class CashbackProcessorTest {

  @Test
  public void savesCashbackData() throws Exception {
    String cashbackTargetId = "client@domain.com";
    OfferStat stat = new OfferStat()
        .setCashbackTargetId(cashbackTargetId);
    Token token = new Token(stat);
    User user = new User();
    OfferAction action = new OfferAction()
        .setId(1L)
        .setAffiliate(user);
    ProcessableData data = new ProcessableData()
        .setOfferAction(action)
        .setToken(token);

    Cashbacks cashbacks = mockCashbacks();
    new CashbackProcessor(cashbacks).process(data);

    List<Cashback> cashbackList = cashbacks.list();
    assertEquals(1, cashbackList.size());
    Cashback savedCashback = cashbackList.get(0);
    assertEquals(cashbackTargetId, savedCashback.targetId());
    assertEquals(action, savedCashback.action());
    assertEquals(user, savedCashback.affiliate());
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
    };

  }
}