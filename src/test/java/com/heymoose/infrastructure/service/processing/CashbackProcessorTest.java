package com.heymoose.infrastructure.service.processing;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class CashbackProcessorTest {

  private static class Cashback {

    private String targetId;
    private OfferAction action;

    public String targetId() {
      return targetId;
    }

    public OfferAction action() {
      return action;
    }

    public Cashback setTargetId(String targetId) {
      this.targetId = targetId;
      return this;
    }

    public Cashback setAction(OfferAction action) {
      this.action = action;
      return this;
    }
  }

  private interface Cashbacks {
    List<Cashback> list();
    Cashback add(Cashback cashback);
  }

  private static class CashbackProcessor implements Processor {

    private final Cashbacks cashbacks;

    private CashbackProcessor(Cashbacks cashbacks) {
      this.cashbacks = cashbacks;
    }

    @Override
    public void process(ProcessableData data) {
      cashbacks.add(new Cashback()
          .setTargetId(data.token().stat().cashbackTargetId())
          .setAction(data.offerAction()));
    }
  }

  @Test
  public void savesCashbackData() throws Exception {
    String cashbackTargetId = "client@domain.com";
    OfferStat stat = new OfferStat()
        .setCashbackTargetId(cashbackTargetId);
    Token token = new Token(stat);
    OfferAction action = new OfferAction().setId(1L);
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
