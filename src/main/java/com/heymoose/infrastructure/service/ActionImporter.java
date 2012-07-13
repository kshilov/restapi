package com.heymoose.infrastructure.service;

import com.google.common.base.Optional;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.resource.xml.XmlActionInfo;
import com.heymoose.resource.xml.XmlActionInfos;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.resource.api.ApiRequestException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static com.heymoose.infrastructure.util.QueryUtil.appendQueryParam;

@Singleton
public class ActionImporter implements Runnable {

  private final static Logger log = LoggerFactory.getLogger(ActionImporter.class);

  private Map<Long, URL> advUrls;
  private final Tracking tracking;
  private final Repo repo;
  private int period;
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
  public void doImport(long advertiserId, XmlActionInfos actions) throws ApiRequestException {
    log.info("Importing {} actions", actions.actions.size());
    for (XmlActionInfo action : actions.actions) {
      Token token = repo.byHQL(Token.class, "from Token where value = ?", action.token);
      if (token == null) {
        log.warn("Token {} not found, skiping", action.token);
        continue;
        //throw notFound(Token.class, token);
      }

      // check whether token was not tracked already
      OfferAction offerAction = repo.byHQL(OfferAction.class, "from OfferAction where token = ?", token);
      if (offerAction != null) {
        log.warn("Token {} was already converted, offerAction={}, skiping", action.token, offerAction.id());
        continue;
      }

      BaseOffer offer = offerLoader.findOffer(advertiserId, action.offerCode);
      Optional<Double> price = action.price != null
          ? Optional.of(action.price)
          : Optional.<Double>absent();
      tracking.trackConversion(token, action.transactionId, Collections.singletonMap(offer, price));
    }
  }

  private static XmlActionInfos getActions(URL url) throws JAXBException {
    JAXBContext jaxb = JAXBContext.newInstance(XmlActionInfos.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    return (XmlActionInfos) unmarshaller.unmarshal(url);
  }

  private static URL addStartTime(URL url, int period) throws URISyntaxException, MalformedURLException {
    URI uri = url.toURI();
    DateTime startTime = DateTime.now().minusHours(period).minusMinutes(10);
    long startTimeSeconds = Math.round(startTime.getMillis() / 1000.0);
    uri = appendQueryParam(uri, "start-time", startTimeSeconds);
    return uri.toURL();
  }

  public void setPeriod(int period) {
    this.period = period;
  }

  public void setAdvUrls(Map<Long, URL> advUrls) {
    this.advUrls = advUrls;
  }

  public Map<Long, URL> advUrls() {
    return advUrls;
  }
}
