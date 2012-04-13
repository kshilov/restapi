package com.heymoose.domain.affiliate;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.Repo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class OfferStats {

  private final Provider<Session> sessionProvider;
  private final Repo repo;

  @Inject
  public OfferStats(Provider<Session> sessionProvider, Repo repo) {
    this.sessionProvider = sessionProvider;
    this.repo = repo;
  }

  public static enum Dir {
    ASC, DESC
  }

  public static enum Ordering {
    OFFER("offer_id"),
    SHOWS("shows"),
    CLICKS("clicks"),
    LEADS("leads"),
    SALES("sales"),
    CONFIRMED_REVENUE("confirmed_revenue"),
    NOT_CONFIRMED_REVENUE("not_confirmed_revenue"),
    CANCELED_REVENUE("canceled_revenue"),
    CTR("ctr"),
    CR("cr"),
    ECPC("ecpc"),
    ECPM("ecpm");

    public final String value;
    private Ordering(String value) {
      this.value = value;
    }
  }

  public List<OverallOfferStats> affStats(long affId, Ordering ordering, Dir dir, int offset, int limit) {
    String query = "with offers as (\n" +
        "\twith parent_offer as (\n" +
        "\t\tselect g.offer_id main, g.offer_id offer_id\n" +
        "\t\tfrom offer_grant g\n" +
        "\t\tleft join offer o on g.offer_id = o.id\n" +
        "\t\twhere\tg.state = 'APPROVED'\n" +
        "\t\t\t\tand g.aff_id = {affId}\n" +
        "\t\t\t\tand o.type = 1\n" +
        "\t)\n" +
        "\tselect p.main, p.offer_id\n" +
        "\tfrom parent_offer p\n" +
        "\tunion all\n" +
        "\tselect o.parent_id main, o.id offer_id\n" +
        "\tfrom offer o\n" +
        "\tinner join parent_offer p on o.parent_id = p.offer_id\n" +
        "), stats as (\n" +
        "\tselect\toffers.main offer_id,\n" +
        "\t\t\tsum(s.show_count) shows,\n" +
        "\t\t\tsum(s.click_count) clicks\n" +
        "\tfrom offers\n" +
        "\tleft join offer_stat s on offers.offer_id = s.offer_id\n" +
        "\tgroup by offers.main\n" +
        "), leads as (\n" +
        "\tselect offers.main offer_id, count(a.id) leads\n" +
        "\tfrom offers\n" +
        "\tinner join offer o on offers.offer_id = o.id\n" +
        "\tleft join offer_action a on offers.offer_id = a.offer_id\n" +
        "\tinner join offer_stat s on a.stat_id = s.id\n" +
        "\twhere o.cpa_policy = 'FIXED' and s.aff_id = {affId}\n" +
        "\tgroup by offers.main\n" +
        "), sales as (\n" +
        "\tselect offers.main offer_id, count(a.id) sales\n" +
        "\tfrom offers\n" +
        "\tinner join offer o on offers.offer_id = o.id\n" +
        "\tleft join offer_action a on offers.offer_id = a.offer_id\n" +
        "\tinner join offer_stat s on a.stat_id = s.id\n" +
        "\twhere o.cpa_policy = 'PERCENT' and s.aff_id = {affId}\n" +
        "\tgroup by offers.main\n" +
        "), not_confirmed_revenue as (\n" +
        "\tselect offers.main offer_id, sum(e.amount) not_confirmed_revenue\n" +
        "\tfrom offers\n" +
        "\tleft join offer_action a on offers.offer_id = a.offer_id\n" +
        "\tinner join accounting_entry e on a.id = e.source_id\n" +
        "\twhere e.event = 1 and e.account_id = {notConfirmedAcc}\n" +
        "\tgroup by offers.main\n" +
        "), confirmed_revenue as (\n" +
        "\tselect offers.main offer_id, sum(e.amount) confirmed_revenue\n" +
        "\tfrom offers\n" +
        "\tleft join offer_action a on offers.offer_id = a.offer_id\n" +
        "\tinner join accounting_entry e on a.id = e.source_id\n" +
        "\twhere e.event = 1 and e.account_id = {confirmedAcc}\n" +
        "\tgroup by offers.main\n" +
        "), canceled_revenue as (\n" +
        "\tselect offers.main offer_id, sum(-e.amount) canceled_revenue\n" +
        "\tfrom offers\n" +
        "\tleft join offer_action a on offers.offer_id = a.offer_id\n" +
        "\tinner join accounting_entry e on a.id = e.source_id\n" +
        "\twhere e.event = 4 and e.account_id = {notConfirmedAcc}\n" +
        "\tgroup by offers.main\n" +
        ")\n" +
        "select\tstats.offer_id,\n" +
        "\t\tstats.shows,\n" +
        "\t\tstats.clicks,\n" +
        "\t\tleads.leads,\n" +
        "\t\tsales.sales,\n" +
        "\t\tconfirmed_revenue.confirmed_revenue,\n" +
        "\t\tnot_confirmed_revenue.not_confirmed_revenue,\n" +
        "\t\tcanceled_revenue.canceled_revenue,\n" +
        "\t\tcase \n" +
        "\t\t\twhen stats.shows = 0 then null\n" +
        "\t\t\telse stats.clicks * 100.0 / stats.shows\n" +
        "\t\tend ctr,\n" +
        "\t\tcase\n" +
        "\t\t\twhen stats.clicks = 0 then null\n" +
        "\t\t\telse (leads.leads + sales.sales) * 100.0 / stats.clicks\n" +
        "\t\tend cr,\n" +
        "\t\tcase\n" +
        "\t\t\twhen stats.clicks = 0 then null\n" +
        "\t\t\telse (confirmed_revenue.confirmed_revenue + not_confirmed_revenue.not_confirmed_revenue) * 1.0 / stats.clicks\n" +
        "\t\tend ecpc,\n" +
        "\t\tcase\n" +
        "\t\t\twhen stats.shows = 0 then null\n" +
        "\t\t\telse (confirmed_revenue.confirmed_revenue + not_confirmed_revenue.not_confirmed_revenue) * 1000 / stats.shows\n" +
        "\t\tend ecpm\n" +
        "from stats\n" +
        "left join leads using(offer_id)\n" +
        "left join sales using(offer_id)\n" +
        "left join confirmed_revenue using(offer_id)\n" +
        "left join not_confirmed_revenue using(offer_id)\n" +
        "left join canceled_revenue using(offer_id)\n" +
        "order by {ordering} {dir}\n" +
        "offset {offset} limit {limit}";

    User affiliate = repo.get(User.class, affId);
    long notConfirmedAccId = affiliate.affiliateAccountNotConfirmed().id();
    long confirmedAccId = affiliate.affiliateAccount().id();

    query = query.replaceAll("\\{affId\\}", Long.toString(affId));
    query = query.replaceAll("\\{notConfirmedAcc\\}", Long.toString(notConfirmedAccId));
    query = query.replaceAll("\\{confirmedAcc\\}", Long.toString(confirmedAccId));
    query = query.replaceAll("\\{ordering\\}", ordering.value);
    query = query.replaceAll("\\{dir\\}", dir.name());
    query = query.replaceAll("\\{offset\\}", Integer.toString(offset));
    query = query.replaceAll("\\{limit\\}", Integer.toString(limit));

    System.out.println(query);

    List<Object[]> records = sessionProvider.get().createSQLQuery(query).list();
    List<OverallOfferStats> stats = newArrayList();

    for (Object[] record : records) {
      stats.add(new OverallOfferStats(
          extractLong(record[0]),
          extractLong(record[1]),
          extractLong(record[2]),
          extractLong(record[3]),
          extractLong(record[4]),
          extractDouble(record[5]),
          extractDouble(record[6]),
          extractDouble(record[7]),
          extractDoubleOrNull(record[8]),
          extractDoubleOrNull(record[9]),
          extractDoubleOrNull(record[10]),
          extractDoubleOrNull(record[11])
      ));
    }
    return stats;
  }

  public long countAffStats(long affId) {
    String query = "with offers as (\n" +
        "\twith parent_offer as (\n" +
        "\t\tselect g.offer_id offer_id\n" +
        "\t\tfrom offer_grant g\n" +
        "\t\tleft join offer o on g.offer_id = o.id\n" +
        "\t\twhere\tg.state = 'APPROVED'\n" +
        "\t\t\t\tand g.aff_id = {affId}\n" +
        "\t\t\t\tand o.type = 1\n" +
        "\t)\n" +
        "\tselect p.offer_id\n" +
        "\tfrom parent_offer p\n" +
        "\tunion all\n" +
        "\tselect o.id offer_id\n" +
        "\tfrom offer o\n" +
        "\tinner join parent_offer p on o.parent_id = p.offer_id\n" +
        ")\n" +
        "select count(*) from offers;";
    query = query.replaceAll("\\{affId\\}", Long.toString(affId));
    return extractLong(sessionProvider.get().createSQLQuery(query).uniqueResult());
  }

  private static Long extractLong(Object val) {
    if (val == null)
      return 0L;
    if (val instanceof BigInteger)
      return ((BigInteger) val).longValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).longValue();
    throw new IllegalStateException();
  }

  private static double extractDouble(Object val) {
    if (val == null)
      return 0.0;
    if (val instanceof BigInteger)
      return ((BigInteger) val).doubleValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).doubleValue();
    throw new IllegalStateException();
  }

  private static Double extractDoubleOrNull(Object val) {
    if (val == null)
      return null;
    if (val instanceof BigInteger)
      return ((BigInteger) val).doubleValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).doubleValue();
    throw new IllegalStateException();
  }
}
