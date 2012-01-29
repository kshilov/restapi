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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

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
  public XmlStats ctr(@QueryParam("from") Long from,
                      @QueryParam("to") Long to,
                      @QueryParam("offerId") Long offerId,
                      @QueryParam("appId") Long appId,
                      @QueryParam("performerId") Long performerId,
                      @QueryParam("trunc") String trunc) {

    checkNotNull(from, to);
    checkArgument(asList("hour", "month", "year", "day").contains(trunc));

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
  
  @GET
  @Path("ctr-by-ids")
  @Transactional
  public XmlStats ctrByIds(@QueryParam("from") Long from,
                           @QueryParam("to") Long to,
                           @QueryParam("offer") List<Long> offerIds,
                           @QueryParam("app") List<Long> appIds) {
    if (offerIds.isEmpty() == appIds.isEmpty())
      throw new WebApplicationException(400);
    
    DateTime dtFrom = from != null ? new DateTime(from) : null;
    DateTime dtTo = to != null ? new DateTime(to) : null;
    List<Long> ids = null;
    Map<Long, Integer> showCounts = null;
    Map<Long, Integer> actionCounts = null;
    
    if (offerIds.isEmpty()) {
      ids = appIds;
      showCounts = shows.countByApps(appIds, dtFrom, dtTo);
      actionCounts = actions.countByApps(appIds, dtFrom, dtTo);
    }
    else {
      ids = offerIds;
      showCounts = shows.countByOffers(offerIds, dtFrom, dtTo);
      actionCounts = actions.countByOffers(offerIds, dtFrom, dtTo);
    }
    
    XmlStats xmlStats = new XmlStats();
    for (Long id : ids) {
      Integer shows = showCounts.get(id);
      if (shows == null) shows = 0;
      Integer actions = actionCounts.get(id);
      if (actions == null) actions = 0;
      
      XmlStat xmlStat = new XmlStat();
      xmlStat.id = id;
      xmlStat.shows = shows;
      xmlStat.actions = actions;
      xmlStat.ctr = shows > 0 ? (double)actions / shows : 0;
      
      xmlStats.stats.add(xmlStat);
    }
    
    return xmlStats;
  }
  
  @GET
  @Path("audience/by-genders")
  @Transactional
  public XmlStats audienceByGenders(@QueryParam("offerId") Long offerId,
                                    @QueryParam("appId") Long appId,
                                    @QueryParam("from") Long from,
                                    @QueryParam("to") Long to) {
    DateTime dtFrom = from != null ? new DateTime(from) : null;
    DateTime dtTo = to != null ? new DateTime(to) : null;
    
    Map<Boolean, Integer> counts = actions.audienceByGenders(offerId, appId, dtFrom, dtTo);
    List<Boolean> genders = newArrayList(counts.keySet());
    
    XmlStats xmlStats = new XmlStats();
    for (Boolean gender : genders) {
      XmlStat xmlStat = new XmlStat();
      xmlStat.gender = gender;
      xmlStat.performers = counts.get(gender);
      xmlStats.stats.add(xmlStat);
    }
    
    Collections.sort(xmlStats.stats, new Comparator<XmlStat>() {
      public int compare(XmlStat one, XmlStat other) {
        return -one.performers.compareTo(other.performers);
      }
    });
    
    return xmlStats;
  }
  
  @GET
  @Path("audience/by-cities")
  @Transactional
  public XmlStats audienceByCities(@QueryParam("offerId") Long offerId,
                                   @QueryParam("appId") Long appId,
                                   @QueryParam("from") Long from,
                                   @QueryParam("to") Long to) {
    DateTime dtFrom = from != null ? new DateTime(from) : null;
    DateTime dtTo = to != null ? new DateTime(to) : null;
    
    Map<String, Integer> counts = actions.audienceByCities(offerId, appId, dtFrom, dtTo);
    List<String> cities = newArrayList(counts.keySet());
    
    XmlStats xmlStats = new XmlStats();
    for (String city : cities) {
      XmlStat xmlStat = new XmlStat();
      xmlStat.city = city;
      xmlStat.performers = counts.get(city);
      xmlStats.stats.add(xmlStat);
    }
    
    Collections.sort(xmlStats.stats, new Comparator<XmlStat>() {
      public int compare(XmlStat one, XmlStat other) {
        return -one.performers.compareTo(other.performers);
      }
    });
    
    return xmlStats;
  }
  
  @GET
  @Path("audience/by-years")
  @Transactional
  public XmlStats audienceByYears(@QueryParam("offerId") Long offerId,
                                  @QueryParam("appId") Long appId,
                                  @QueryParam("from") Long from,
                                  @QueryParam("to") Long to) {
    DateTime dtFrom = from != null ? new DateTime(from) : null;
    DateTime dtTo = to != null ? new DateTime(to) : null;
    
    Map<Integer, Integer> counts = actions.audienceByYears(offerId, appId, dtFrom, dtTo);
    List<Integer> years = newArrayList(counts.keySet());
    
    XmlStats xmlStats = new XmlStats();
    for (Integer year : years) {
      XmlStat xmlStat = new XmlStat();
      xmlStat.year = year;
      xmlStat.performers = counts.get(year);
      xmlStats.stats.add(xmlStat);
    }
    
    Collections.sort(xmlStats.stats, new Comparator<XmlStat>() {
      public int compare(XmlStat one, XmlStat other) {
        if (one.year == null) return 1;
        if (other.year == null) return -1;
        return -one.year.compareTo(other.year);
      }
    });
    
    return xmlStats;
  }
}
