package com.heymoose.domain;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.heymoose.hibernate.Transactional;
import org.hibernate.Session;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
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

  private final Provider<Session> sessionProvider;
  private final Properties settings;

  @Inject
  public Mlm(Provider<Session> sessionProvider, @Named("settings") Properties settings) {
    this.sessionProvider = sessionProvider;
    this.settings = settings;
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

  @XmlRootElement(name = "report")
  public static class XmlReport {
    @XmlElement(name = "performer")
    public List<XmlReportItem> items = newArrayList();
  }

  @XmlRootElement(name = "performer")
  public static class XmlReportItem {
    @XmlElement(name = "id")
    public Long id;

    @XmlElement(name = "parent-id")
    public Long pid;

    @XmlElement(name = "revenue")
    public String revenue;
  }

  public String toXml(Iterable<Node> nodes) {
    try {
      XmlReport xmlReport = new XmlReport();
      for (Node node : nodes) {
        XmlReportItem item = new XmlReportItem();
        item.id = node.id;
        item.pid = node.pid;
        item.revenue = node.revenue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
        xmlReport.items.add(item);
      }
      JAXBContext context = JAXBContext.newInstance(XmlReport.class, XmlReportItem.class);
      Marshaller marshaller = context.createMarshaller();
      StringWriter sw = new StringWriter();
      marshaller.marshal(xmlReport, sw);
      return sw.toString();
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public void doMlmExport(DateTime startTime) {
    DateTime toTime = startTime;
    DateTime fromTime = toTime.minusDays(1);

    String sql = "with _tmp as ( " +
        "select p.id id, p.inviter pid, " +
        "(select balance from account_tx t  where t.id = a.reservation order by version desc limit 1) amount  " +
        "from action a inner join performer p on a.performer_id = p.id  " +
        "where a.approve_time between :fromTime and :toTime and a.done) " +
        "select _tmp.id, _tmp.pid, sum(_tmp.amount) from _tmp group by _tmp.id, _tmp.pid";

    List<Object[]> records = hiber().createSQLQuery(sql)
        .setTimestamp("fromTime", fromTime.toDate())
        .setTimestamp("toTime", toTime.toDate())
        .list();

    List<Node> nodes = newArrayList();
    for (Object[] record : records)
      nodes.add(toNode(record));

    calcPassiveRevenue(nodes, tax());
    System.out.println(toXml(nodes));
  }

  private Node toNode(Object[] record) {
    long id =  ((BigInteger) record[0]).longValue();
    Long pid = record[1] == null ? null : ((BigInteger) record[0]).longValue();
    BigDecimal amount = ((BigDecimal) record[2]);
    amount = amount.subtract(amount.multiply(new BigDecimal(compensation())));
    return node(id, pid, amount);
  }

  private static class Node {

    public long id;
    public Long pid;

    public BigDecimal amount;
    public BigDecimal revenue = new BigDecimal("0.0");
    public int count;

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("id", id)
          .add("pid", pid)
          .add("count", count)
          .add("amount", amount.setScale(2, BigDecimal.ROUND_HALF_EVEN))
          .add("revenue", revenue.setScale(2, BigDecimal.ROUND_HALF_EVEN))
          .toString();
    }
  }

  private static void calcPassiveRevenue(Iterable<Node> nodes, double tax) {
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
//     Iterable<Node> nodes = asList(
//        node(1, null, amount(1.0)),
//        node(2, 1L, amount(1.0)),
//        node(3, 1L, amount(1.0)),
//        node(4, 1L, amount(1.0)),
//        node(5, 4L, amount(1.0)),
//        node(6, 4L, amount(1.0))
//    );

    List<Node> nodes = Lists.newArrayList();
//    for (long i = 1; i < 10000; i++)
//      nodes.add(node(i, i - 1, amount(1.0)));

    calcPassiveRevenue(nodes, 0.1);

//    for (Node node : nodes)
//      System.out.println(node);

  }
}
