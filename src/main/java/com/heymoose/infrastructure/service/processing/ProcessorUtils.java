package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.util.QueryUtil;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public final class ProcessorUtils {

  private static final Logger log =
      LoggerFactory.getLogger(ProcessorUtils.class);

  private ProcessorUtils() { }

  public static OfferStat copyStat(OfferStat source, BaseOffer offer) {
    return new OfferStat(
            source.bannerId(),
            offer.id(),
            offer.master(),
            source.affiliate().id(),
            source.sourceId(),
            source.subs(),
            source.referer(),
            source.keywords())
        .setCashbackTargetId(source.cashbackTargetId())
        .setCashbackReferrer(source.cashbackReferrer());
  }

  public static void doPostBack(String url, OfferAction action) {
    try {
      if (url != null) {
        getRequest(makeFullPostBackUri(
            URI.create(url),
            action.stat().sourceId(),
            action.stat().subs(),
            action.stat().referer(),
            action.stat().keywords(),
            action.offer().id(),
            action.token().affParams()));
      }
    } catch (Exception e) {
      log.warn("Error while requesting postBackUrl: " + url, e);
    }

  }

  public static OfferAction findAction(Repo repo, BaseOffer offer, Token token) {
    return repo.byHQL(OfferAction.class,
        "from OfferAction where offer = ? and token = ?",
        offer ,token);
  }

  public static OfferAction findAction(Repo repo, BaseOffer offer, Token token,
                                       String transactionId) {
    return repo.byHQL(OfferAction.class,
        "from OfferAction where offer = ? and token = ? and transactionId = ?",
        offer, token, transactionId);
  }

  private static URI makeFullPostBackUri(
      URI uri, String sourceId, Subs subs, String referer, String keywords,
      long offerId, Map<String, String> affParams) {

    if (sourceId != null)
      uri = QueryUtil.appendQueryParam(uri, "source_id", sourceId);
    if (subs.subId() != null)
      uri = QueryUtil.appendQueryParam(uri, "sub_id", subs.subId());
    if (subs.subId1() != null)
      uri = QueryUtil.appendQueryParam(uri, "sub_id1", subs.subId1());
    if (subs.subId2() != null)
      uri = QueryUtil.appendQueryParam(uri, "sub_id2", subs.subId2());
    if (subs.subId3() != null)
      uri = QueryUtil.appendQueryParam(uri, "sub_id3", subs.subId3());
    if (subs.subId4() != null)
      uri = QueryUtil.appendQueryParam(uri, "sub_id4", subs.subId4());
    if (referer != null)
      uri = QueryUtil.appendQueryParam(uri, "referer", referer);
    if (keywords != null)
      uri = QueryUtil.appendQueryParam(uri, "keywords", keywords);
    uri = QueryUtil.appendQueryParam(uri, "offer_id", offerId);
    for (Map.Entry<String, String> ent : affParams.entrySet())
      uri = QueryUtil.appendQueryParam(uri, ent.getKey(), ent.getValue());
    return uri;
  }

  private static void getRequest(URI uri) {
    InputStream is = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      URL url = new URL(uri.toString());
      is = url.openStream();
      IOUtils.copy(is, baos);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      if (is != null)
        try {
          is.close();
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
    }
  }

}
