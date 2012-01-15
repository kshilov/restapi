package com.heymoose.resource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.OfferShowRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.XmlStat;
import com.heymoose.resource.xml.XmlStats;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.joda.time.DateTime;

@Path("stats")
@Singleton
public class StatsResource {

  private final OfferShowRepository shows;
  private final ActionRepository actions;

  @Inject
  public StatsResource(OfferShowRepository shows, ActionRepository actions) {
    this.shows = shows;
    this.actions = actions;
  }

  @GET
  @Path("ctr")
  @Transactional
  public XmlStats get(@QueryParam("from") Long from,
                      @QueryParam("to") Long to,
                      @QueryParam("offerId") Long offerId,
                      @QueryParam("appId") Long appId,
                      @QueryParam("performerId") Long performerId,
                      @QueryParam("trunc") String trunc) {

    checkNotNull(from, to);
    checkArgument(asList("hour", "month", "year").contains(trunc));

    DateTime dtFrom = new DateTime(from);
    DateTime dtTo = new DateTime(to);

    Map<DateTime, Integer> showStats = shows.stats(dtFrom, dtTo, offerId, appId, performerId, trunc);
    Map<DateTime, Integer> actionStats = actions.stats(dtFrom, dtTo, offerId, appId, performerId, trunc);

    List<DateTime> times = newArrayList(showStats.keySet());
    Collections.sort(times);

    XmlStats xmlStats = new XmlStats();
    for (DateTime time : times) {
      XmlStat xmlStat = new XmlStat();
      xmlStat.time = time.getMillis();
      xmlStat.shows = showStats.get(time);
      Integer actions = actionStats.get(time);
      actions = (actions == null) ? 0 : actions;
      xmlStat.actions = actions;
      xmlStat.ctr = (double) actions / xmlStat.shows;
      xmlStats.stats.add(xmlStat);
    }

    return xmlStats;
  }
}
