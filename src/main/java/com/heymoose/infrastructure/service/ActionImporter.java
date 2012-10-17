package com.heymoose.infrastructure.service;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import com.heymoose.infrastructure.service.processing.Processor;
import com.heymoose.resource.api.ApiRequestException;
import com.heymoose.resource.xml.XmlActionInfo;
import com.heymoose.resource.xml.XmlActionInfos;
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
import java.util.Map;

import static com.heymoose.infrastructure.util.QueryUtil.appendQueryParam;

@Singleton
public class ActionImporter implements Runnable {

  private final static Logger log = LoggerFactory.getLogger(ActionImporter.class);

  private Map<Long, URL> advUrls;
  private final Repo repo;
  private int period;
  private final OfferLoader offerLoader;
  private Processor actionProcessor;

  @Inject
  public ActionImporter(@Named("adv-map") Map<Long, URL> advUrls,
                        Processor processor,
                        Repo repo,
                        @Named("action-import-period") int period,
                        OfferLoader offerLoader) {
    this.advUrls = advUrls;
    this.actionProcessor = processor;
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
      ProcessableData data = action.toProcessableData().setOffer(offer);
      actionProcessor.process(data);
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
