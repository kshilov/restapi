package com.heymoose.domain;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.heymoose.hibernate.Transactional;
import com.heymoose.job.StartTimeCalculator;
import org.hibernate.Session;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

@Singleton
public class MlmExport {

  private final Provider<Session> sessionProvider;
  private final int runAtHours;
  private final int runAtMinutes;

  @Inject
  public MlmExport(Provider<Session> sessionProvider, int runAtHours, int runAtMinutes) {
    this.sessionProvider = sessionProvider;
    this.runAtHours = runAtHours;
    this.runAtMinutes = runAtMinutes;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  public void perform() {
    //FIXME
    DateTime toTime = StartTimeCalculator.calcStartTime(runAtHours, runAtMinutes);
    DateTime fromTime = toTime.minusDays(1);

    String sql = "with _tmp as (\n" +
        "select p.id id, p.inviter pid,\n" +
        "(select balance from account_tx t  where t.id = a.reservation order by version desc limit 1) amount \n" +
        "from action a inner join performer p on a.performer_id = p.id \n" +
        "where a.creation_time between :fromTime and :toTime' and a.done)\n" +
        "select (_tmp.id, _tmp.pid), sum(_tmp.amount) from _tmp group by (_tmp.id, _tmp.pid);";
  }

  private static class Node {
    public long id;
    public Long pid;
    public BigDecimal amount;
    public int count;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Node node = (Node) o;

      if (id != node.id) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("id", id)
          .add("pid", pid)
          .add("count", count)
          .add("amount", amount.setScale(2, BigDecimal.ROUND_HALF_EVEN))
          .toString();
    }
  }

  private static Map<Long, Node> buildMap(Iterable<Node> nodes) {
    Map<Long, Node> map = Maps.newLinkedHashMap();
    for (Node node : nodes)
      map.put(node.id, node);
    return map;
  }

  private static void calcCounters(Map<Long, Node> map) {
    for (Node node : map.values())
      if (node.pid != null)
        map.get(node.pid).count++;
  }

  private static List<Node> sort(Map<Long, Node> map) {
    List<Node> L = Lists.newArrayList();
    Set<Node> S = Sets.newHashSet();
    for (Node node : map.values())
      if (node.count == 0)
        S.add(node);
    while (!S.isEmpty()) {
      Iterator<Node> it = S.iterator();
      Node n = it.next();
      it.remove();
      L.add(n);
      if (n.pid != null) {
        Node m = map.get(n.pid);
        m.count--;
        if (m.count == 0)
          S.add(m);
      }
    }
    return L;
  }

  private static void mlmCount(Map<Long, Node> map, double tax) {
    for (Node node : map.values()) {
      if (node.pid != null) {
        BigDecimal up = node.amount.multiply(new BigDecimal(tax));
        node.amount = node.amount.subtract(up);
        Node parent = map.get(node.pid);
        parent.amount = parent.amount.add(up);
      }
    }
  }

  private static Node node(long id, Long pid, BigDecimal amount) {
    Node node = new Node();
    node.id = id;
    node.pid = pid;
    node.amount = amount;
    return node;
  }

  private static BigDecimal amount(double amount) {
    return new BigDecimal(amount);
  }

  public static void main(String[] args) {
    Iterable<Node> nodes = asList(
        node(1, null, amount(1.0)),
        node(2, 1L, amount(1.0)),
        node(3, 1L, amount(1.0)),
        node(4, 1L, amount(1.0)),
        node(5, 4L, amount(1.0)),
        node(6, 4L, amount(1.0))
    );
    Map<Long, Node> map = buildMap(nodes);
    calcCounters(map);
    List<Node> sorted = sort(map);
    System.out.println(sorted);
    map = buildMap(sorted);
    mlmCount(map, 0.1);

    for (Node node : map.values())
      System.out.println(node);
  }
}
