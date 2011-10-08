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
        node(1, null, "1", 1, amount(2.0)),
        node(2, 1L, "2", 1, amount(1.0)),
        node(3, 1L, "3", 1, amount(1.0)),
        node(4, 1L, "4", 1, amount(1.0)),
        node(5, 4L, "5", 1, amount(1.0)),
        node(6, 4L, "6", 1, amount(1.0)),

        node(21, null, "1", 2, amount(2.0)),
        node(22, 21L, "2", 2, amount(1.0)),
        node(23, 21L, "3", 2, amount(1.0)),
        node(24, 21L, "4", 2, amount(1.0)),
        node(25, 24L, "5", 2, amount(1.0)),
        node(26, 24L, "6", 2, amount(1.0))
    );
    Mlm.calcPassiveRevenue(nodes, 0.1);
  }
}
