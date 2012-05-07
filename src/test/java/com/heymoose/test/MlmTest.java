package com.heymoose.test;

import com.heymoose.domain.Mlm;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Iterator;

import static com.heymoose.domain.Mlm.amount;
import static com.heymoose.domain.Mlm.node;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MlmTest {
  @Test public void calcRevenue() {
    Iterable<Mlm.Node> nodes = asList(
        node(1, null, null),
        node(2, 1L, null),
        node(3, 2L, amount(50)),
        node(4, 3L, amount(100))
    );
    Mlm.calcPassiveRevenue(nodes);
    for (Mlm.Node n : nodes)
      System.out.println(n);
  }
}
