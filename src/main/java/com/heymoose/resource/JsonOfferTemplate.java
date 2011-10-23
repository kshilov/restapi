package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.Offer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.math.BigDecimal;

import static com.heymoose.domain.Compensation.subtractCompensation;

public class JsonOfferTemplate implements OfferTemplate {

  @Override
  public String render(Iterable<Offer> offers, App app, String extId, BigDecimal compensation) {
    try {
      ObjectMapper mapper = MAPPER.get();
      ObjectNode jsResult = mapper.createObjectNode();
      ArrayNode jsOffers = mapper.createArrayNode();
      for (Offer offer : offers) {
        ObjectNode jsOffer = mapper.createObjectNode();
        jsOffer.put("id", offer.id());
        jsOffer.put("title", offer.title());
        jsOffer.put("description", offer.description());
        BigDecimal payment = subtractCompensation(offer.order().cpa(), compensation);
        jsOffer.put("payment", payment.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        jsOffer.put("image", offer.imageBase64());
        jsOffers.add(jsOffer);
      }
      jsResult.put("success", true);
      jsResult.put("result", jsOffers);
      return mapper.writeValueAsString(jsResult);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final static ThreadLocal<ObjectMapper> MAPPER = new ThreadLocal<ObjectMapper>() {
    @Override
    protected ObjectMapper initialValue() {
      return new ObjectMapper();
    }
  };
}
