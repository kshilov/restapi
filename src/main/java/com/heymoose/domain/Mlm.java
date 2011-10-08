package com.heymoose.domain;

import com.google.common.collect.Maps;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rabbitmq.RabbitMqSender;
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

@Singleton
public class Mlm {

  private final static Logger log = LoggerFactory.getLogger(Mlm.class);

  private final Provider<Session> sessionProvider;
  private final Properties settings;
  private final RabbitMqSender mqSender;

  @Inject
  public Mlm(Provider<Session> sessionProvider,
             @Named("settings") Properties settings,
             RabbitMqSender mqSender) {
    this.sessionProvider = sessionProvider;
    this.settings = settings;
    this.mqSender = mqSender;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  private double compensation() {
    return Double.parseDouble(settings.getProperty("compensation"));
  }

  private double tax() {
    return Double.parseDouble(settings.getProperty("tax"));
  }

  public static class Report {
    public DateTime fromTime;
    public DateTime toTime;
    public Long appId;
    public String callback;
    public List<ReportItem> items = newArrayList();
  }

  public static class ReportItem {
    public String extId;
    public String passiveRevenue;
  }

  public Iterable<Report> generateReports(Iterable<Node> nodes, DateTime fromTime, DateTime toTime) {
    List<Report> reports = newArrayList();
    Report report = new Report();
    Long prevAppId = null;
    for (Node node : nodes) {
      if (prevAppId != null && !Long.valueOf(node.appId).equals(prevAppId)) {
        report.appId = prevAppId;
        report.callback = callbackFor(prevAppId);
        report.fromTime = fromTime;
        report.toTime = toTime;
        if (!report.items.isEmpty())
          reports.add(report);
        report = new Report();
      }
      if (node.revenue.compareTo(new BigDecimal(0.0)) != 0) {
        ReportItem item = new ReportItem();
        item.extId = node.extId;
        item.passiveRevenue = node.revenue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
        report.items.add(item);
        prevAppId = node.appId;
      }
    }
    if (prevAppId != null) {
      report.appId = prevAppId;
      report.callback = callbackFor(prevAppId);
      report.fromTime = fromTime;
      report.toTime = toTime;
      if (!report.items.isEmpty())
        reports.add(report);
    }
    return reports;
  }

  @Transactional
  public void doMlmExport(DateTime startTime) {
    DateTime toTime = startTime;
    DateTime fromTime = toTime.minusDays(1);

    log.info("fromTime: {}", fromTime);
    log.info("toTime: {}", toTime);

    String sql = "with recursive _tmp as ( " +
        "select p.id id, p.inviter pid, p.ext_id ext_id, p.app_id app_id, " +
        "(select -diff from account_tx t  where t.id = a.reservation order by version desc limit 1) amount " +
        "from action a inner join performer p on a.performer_id = p.id " +
        "where a.approve_time between :fromTime and :toTime and a.done = true " +
        "union all " +
        "select __tmp.id id, __tmp.inviter pid, __tmp.ext_id ext_id, __tmp.app_id app_id, cast(0 as numeric(19,2)) amount " +
        "from performer __tmp, _tmp where __tmp.id = _tmp.pid) " +
        "select _tmp.id, _tmp.pid, _tmp.ext_id, _tmp.app_id, sum(_tmp.amount) " +
        "from _tmp group by _tmp.id, _tmp.pid, _tmp.ext_id, _tmp.app_id order by _tmp.app_id";

    List<Object[]> records = hiber().createSQLQuery(sql)
        .setTimestamp("fromTime", fromTime.toDate())
        .setTimestamp("toTime", toTime.toDate())
        .list();

    List<Node> nodes = newArrayList();
    for (Object[] record : records)
      nodes.add(toNode(record));

    calcPassiveRevenue(nodes, tax());

    for (Report report : generateReports(nodes, fromTime, toTime))
      trySendReport(report);
  }

  private void trySendReport(Report report) {
    try {
      mqSender.send(toJson(report).getBytes("UTF-8"), "reports", "notify");
    } catch (Exception e) {
      log.error("Failed to publish report", e);
    }
  }

  @Transactional
  private String callbackFor(long appId) {
    App app = (App) hiber().get(App.class, appId);
    return app.callback().toString();
  }

  private static String toJson(Report report) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    json.put("appId", report.appId);
    json.put("callback", report.callback);
    json.put("fromTime", report.fromTime.toString());
    json.put("toTime", report.toTime.toString());
    ArrayNode jsonItems = mapper.createArrayNode();
    for (Mlm.ReportItem item : report.items) {
      ObjectNode jsonItem = mapper.createObjectNode();
      jsonItem.put("extId", item.extId);
      jsonItem.put("passiveRevenue", item.passiveRevenue);
      jsonItems.add(jsonItem);
    }
    json.put("items", jsonItems);
    try {
      return mapper.writeValueAsString(json);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create JSON from report", e);
    }
  }

  private Node toNode(Object[] record) {
    //         0           1             2                 3                          4
    // _tmp.id, _tmp.pid, _tmp.ext_id, _tmp.app_id, sum(_tmp.amount)
    long id =  bigIntegerAsLong(record[0]);
    Long pid = (record[1] == null) ? null : bigIntegerAsLong(record[1]);
    String extId = (String) record[2];
    long appId = bigIntegerAsLong(record[3]);
    BigDecimal amount = ((BigDecimal) record[4]);
    amount = amount.subtract(amount.multiply(new BigDecimal(compensation())));
    return node(id, pid, extId, appId, amount);
  }

  private long bigIntegerAsLong(Object bigInteger) {
    return ((BigInteger) bigInteger).longValue();
  }

  public static class Node {

    public long id;
    public Long pid;
    public String extId;
    public long appId;

    public BigDecimal amount;
    public BigDecimal revenue = new BigDecimal("0.0");
    public int count;
  }

  public static void calcPassiveRevenue(Iterable<Node> nodes, double tax) {
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
      if (parent == null)
        continue;
      parent.count--;
      if (parent.count == 0)
        S.add(parent);
      calcPassiveRevenue(n, parent, tax);
    }
  }

  private static void calcPassiveRevenue(Node node, Node parent, double tax) {
    BigDecimal revenue = node.amount.add(node.revenue).multiply(new BigDecimal(tax));
    node.revenue = node.revenue.subtract(revenue);
    parent.revenue = parent.revenue.add(revenue);
  }

  public static Node node(long id, Long pid, String extId, long appId, BigDecimal amount) {
    Node node = new Node();
    node.id = id;
    node.pid = pid;
    node.extId = extId;
    node.appId = appId;
    node.amount = amount;
    return node;
  }

  public static BigDecimal amount(double amount) {
    return new BigDecimal(amount);
  }
}
