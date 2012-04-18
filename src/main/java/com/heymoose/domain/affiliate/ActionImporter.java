package com.heymoose.domain.affiliate;

import com.google.common.base.Optional;
import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.api.ApiExceptions.notFound;
import com.heymoose.resource.api.ApiRequestException;
import static com.heymoose.util.QueryUtil.appendQueryParam;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ActionImporter implements Runnable {

  private final static Logger log = LoggerFactory.getLogger(ActionImporter.class);

  private final Map<Long, URL> advUrls;
  private final Tracking tracking;
  private final Repo repo;
  private final int period;
  private final OfferLoader offerLoader;

  @Inject
  public ActionImporter(@Named("adv-map") Map<Long, URL> advUrls, Tracking tracking, Repo repo, @Named("action-import-period") int period, OfferLoader offerLoader) {
    this.advUrls = advUrls;
    this.tracking = tracking;
    this.repo = repo;
    this.period = period;
    this.offerLoader = offerLoader;
  }

  @Override
  public void run() {
    for (Map.Entry<Long, URL> ent : advUrls.entrySet()) {
      try {
        URL url = addStartTime(ent.getValue(), period);
        log.info("Importing: {}", url.toString());
        doImport(ent.getKey(), getActions(url));
      } catch (Exception e) {
        log.error("Can't import actions from: " + ent.getValue() + ", advertiser_id=" + ent.getKey(), e);
      }
    }
  }

  @Transactional
  public void doImport(long advertiserId, ActionInfos actions) throws ApiRequestException {
    for (ActionInfo action : actions.actions) {
      Token token = repo.byHQL(Token.class, "from Token where value = ?", action.token);
      if (token == null)
        throw notFound(Token.class, token);
      BaseOffer offer = offerLoader.findOffer(advertiserId, action.offerCode);
      Optional<Double> price = action.price != null
          ? Optional.of(action.price)
          : Optional.<Double>absent();
      tracking.actionDone(token, action.transactionId, Collections.singletonMap(offer, price));
    }
  }

  private static ActionInfos getActions(URL url) throws JAXBException {
    JAXBContext jaxb = JAXBContext.newInstance(ActionInfos.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    return (ActionInfos) unmarshaller.unmarshal(url);
  }

  private static URL addStartTime(URL url, int period) throws URISyntaxException, MalformedURLException {
    URI uri = url.toURI();
    DateTime startTime = DateTime.now().minusHours(period).minusMinutes(10);
    uri = appendQueryParam(uri, "start-time", startTime.getMillis());
    return uri.toURL();
  }
}
