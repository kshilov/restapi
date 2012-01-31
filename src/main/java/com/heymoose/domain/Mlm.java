package com.heymoose.domain;

import static com.google.common.collect.Lists.reverse;
import com.google.common.collect.Maps;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rabbitmq.RabbitMqSender;
import java.util.Iterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import static com.google.common.collect.Lists.newArrayList;
import static com.heymoose.domain.Compensation.subtractCompensation;

@Singleton
public class Mlm {

  private final static Logger log = LoggerFactory.getLogger(Mlm.class);

  private final Provider<Session> sessionProvider;
  private final Accounts accounts;

  @Inject
  public Mlm(Provider<Session> sessionProvider, Accounts accounts) {
    this.sessionProvider = sessionProvider;
    this.accounts = accounts;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  public void doMlmExport(DateTime startTime) {
    DateTime toTime = startTime;
    DateTime fromTime = toTime.minusDays(1);

    log.info("fromTime: {}", fromTime);
    log.info("toTime: {}", toTime);

    String s1 = "select u.id, sum(-t.diff) from action a " +
        "left join account_tx t on t.id = a.reservation " +
        "left join offer_order ord on ord.account_id = t.account_id " +
        "left join user_profile u on u.id = ord.user_id " +
        "where a.approve_time between :fromTime and :toTime group by u.id";

    List<Object[]> r1 = hiber().createSQLQuery(s1)
        .setTimestamp("fromTime", fromTime.toDate())
        .setTimestamp("toTime", toTime.toDate())
        .list();

    if (r1.isEmpty())
      return;

    Map<Long, BigDecimal> user2amount = newHashMap();
    for (Object[] x : r1)
      user2amount.put(bigIntegerAsLong(x[0]), (BigDecimal) x[1]);

    String s2 = "with recursive _tmp as ( " +
        "select u.id id, u.referrer referrer from user_profile u where u.id in (:users) " +
        "union all " +
        "select u.id id, u.referrer referrer from user_profile u, _tmp where u.referrer = _tmp.id) " +
        "select distinct id, referrer from _tmp";

    List<Object[]> r2 = hiber().createSQLQuery(s2)
        .setParameterList("users", user2amount.keySet())
        .list();

    List<Node> nodes = newArrayList();
    for (Object[] record : r2)
      nodes.add(toNode(record, user2amount));

    calcPassiveRevenue(nodes);

    for (Node node : nodes) {
      log.info(node.toString());
      if (node.revenue.compareTo(bdecimal(0)) > 0) {
        User user = (User) hiber().get(User.class, node.id);
        accounts.addToBalance(user.customerAccount(), node.revenue, "MLM");
      }
    }
  }

  private Node toNode(Object[] record, Map<Long, BigDecimal> user2amount) {
    long id =  bigIntegerAsLong(record[0]);
    Long pid = (record[1] == null) ? null : bigIntegerAsLong(record[1]);
    BigDecimal amount = user2amount.get(id);
    return node(id, pid, amount);
  }

  private long bigIntegerAsLong(Object bigInteger) {
    return ((BigInteger) bigInteger).longValue();
  }

  public static class Node {

    public long id;
    public Long pid;

    public BigDecimal amount;
    public BigDecimal revenue = new BigDecimal(0);
    public int count;

    public List<Rem> rems = newArrayList();

    @Override
    public String toString() {
      return "Node{" +
          "id=" + id +
          ", pid=" + pid +
          ", amount=" + amount +
          ", revenue=" + revenue +
          ", count=" + count +
          ", rems=" + rems +
          '}';
    }
  }

  public static class Rem {
    public BigDecimal amount;
    public int ttl;
  }

  public static void calcPassiveRevenue(Iterable<Node> nodes) {
    // build map
    Map<Long, Node> map = Maps.newHashMap();
    for (Node node : nodes)
      map.put(node.id, node);

    // calc children
    for (Node node : nodes) {
      Node parent = map.get(node.pid);
      if (parent == null)
        continue;
      parent.count++;
    }

    // find border items
    Queue<Node> S = new LinkedList<Node>();
    for (Node node : nodes)
      if (node.count == 0)
        S.add(node);

    // main part
    while (!S.isEmpty()) {
      Node n = S.remove();
      Node parent = map.get(n.pid);
      calcPassiveRevenue(n, parent);
      if (parent == null)
        continue;
      parent.count--;
      if (parent.count == 0)
        S.add(parent);
    }
  }

  public static final int[] P = {10, 5, 3, 2};

  private static void calcPassiveRevenue(Node node, Node parent) {
    if (node.amount != null && parent != null) {
      for (int i = 0; i < P.length; i++) {
        Rem rem = new Rem();
        rem.amount = node.amount.multiply(bdecimal(P[i]).divide(bdecimal(100)));
        rem.ttl = i;
        parent.rems.add(rem);
      }
    }
    Iterator<Rem> it = node.rems.iterator();
    while (it.hasNext()) {
      Rem rem = it.next();
      if (rem.ttl == 0) {
        node.revenue = node.revenue.add(rem.amount);
      } else if (parent != null) {
        rem.ttl--;
        parent.rems.add(rem);
      }
      it.remove();
    }
  }

  public static Node node(long id, Long pid, BigDecimal amount) {
    Node node = new Node();
    node.id = id;
    node.pid = pid;
    node.amount = amount;
    return node;
  }

  public static BigDecimal amount(double amount) {
    return new BigDecimal(amount);
  }

  public static BigDecimal bdecimal(int arg) {
    return new BigDecimal(arg);
  }
}
