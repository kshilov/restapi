package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

@Singleton
@Path("stats")
public class OfferStatsResource {

    private final OfferStats stats;

    @Inject
    public OfferStatsResource(OfferStats stats) {
        this.stats = stats;
    }

    @GET
    @Path("offers/all")
    @Transactional
    public OverallOfferStatsList offersAll(@QueryParam("from") @DefaultValue("0") Long from,
                                           @QueryParam("to") Long to,
                                           @QueryParam("granted") @DefaultValue("false") boolean granted,
                                           @QueryParam("offset") @DefaultValue("0") int offset,
                                           @QueryParam("limit") @DefaultValue("2147483647") int limit) {
        if (to == null)
            to = DateTimeUtils.currentTimeMillis();
        OverallOfferStatsList list = new OverallOfferStatsList();
        List<OverallOfferStats> overallOfferStats;
        if (granted)
            overallOfferStats = stats.grantedOfferStatsAll(new DateTime(from), new DateTime(to), offset, limit);
        else
            overallOfferStats = stats.offerStatsAll(new DateTime(from), new DateTime(to), offset, limit);
        for (OverallOfferStats s : overallOfferStats)
            list.stats.add(s);
        if (granted)
            list.count = stats.grantedOfferCountAll(new DateTime(from), new DateTime(to));
        else
            list.count = stats.offerCountAll(new DateTime(from), new DateTime(to));
        return list;
    }

    @GET
    @Path("offers/aff")
    @Transactional
    public OverallOfferStatsList offersAff(@QueryParam("aff_id") Long affId,
                                           @QueryParam("from") @DefaultValue("0") Long from,
                                           @QueryParam("to") Long to,
                                           @QueryParam("offset") @DefaultValue("0") int offset,
                                           @QueryParam("limit") @DefaultValue("2147483647") int limit) {
        checkNotNull(affId);
        if (to == null)
            to = DateTimeUtils.currentTimeMillis();
        OverallOfferStatsList list = new OverallOfferStatsList();
        for (OverallOfferStats s : stats.offerStatsByAff(affId, new DateTime(from), new DateTime(to), offset, limit))
            list.stats.add(s);
        list.count = stats.offerCountByAff(affId, new DateTime(from), new DateTime(to));
        return list;
    }

    @GET
    @Path("offers/adv")
    @Transactional
    public OverallOfferStatsList offersAdv(@QueryParam("adv_id") Long advId,
                                           @QueryParam("from") @DefaultValue("0") Long from,
                                           @QueryParam("to") Long to,
                                           @QueryParam("offset") @DefaultValue("0") int offset,
                                           @QueryParam("limit") @DefaultValue("2147483647") int limit) {
        checkNotNull(advId);
        if (to == null)
            to = DateTimeUtils.currentTimeMillis();
        OverallOfferStatsList list = new OverallOfferStatsList();
        for (OverallOfferStats s : stats.offerStatsByAdv(advId, new DateTime(from), new DateTime(to), offset, limit))
            list.stats.add(s);
        list.count = stats.offerCountByAdv(advId, new DateTime(from), new DateTime(to));
        return list;
    }

    @GET
    @Path("affiliates/all")
    @Transactional
    public OverallOfferStatsList affAll(@QueryParam("from") @DefaultValue("0") Long from,
                                        @QueryParam("to") Long to,
                                        @QueryParam("offset") @DefaultValue("0") int offset,
                                        @QueryParam("limit") @DefaultValue("2147483647") int limit) {
        if (to == null)
            to = DateTimeUtils.currentTimeMillis();
        OverallOfferStatsList list = new OverallOfferStatsList();
        for (OverallOfferStats s : stats.affStats(new DateTime(from), new DateTime(to), offset, limit))
            list.stats.add(s);
        list.count = stats.affCount(new DateTime(from), new DateTime(to));
        return list;
    }

    @GET
    @Path("affiliates/offer")
    @Transactional
    public OverallOfferStatsList affOffers(@QueryParam("offer_id") Long offerId,
                                           @QueryParam("from") @DefaultValue("0") Long from,
                                           @QueryParam("to") Long to,
                                           @QueryParam("offset") @DefaultValue("0") int offset,
                                           @QueryParam("limit") @DefaultValue("2147483647") int limit) {
        checkNotNull(offerId);
        to = DateTimeUtils.currentTimeMillis();
        OverallOfferStatsList list = new OverallOfferStatsList();
        for (OverallOfferStats s : stats.affStatsByOffer(offerId, new DateTime(from), new DateTime(to), offset, limit))
            list.stats.add(s);
        list.count = stats.affCountByOffer(offerId, new DateTime(from), new DateTime(to));
        return list;
    }
}
