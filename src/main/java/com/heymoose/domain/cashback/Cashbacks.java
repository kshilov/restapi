package com.heymoose.domain.cashback;

import java.util.List;

public interface Cashbacks {
  List<Cashback> list();
  Cashback add(Cashback cashback);
}
