package com.heymoose.resource;

import com.heymoose.resource.api.data.OfferData;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JsonOfferTemplate implements OfferTemplate {

  @Override
  public String render(Iterable<OfferData> offers) {
    try {
      ObjectMapper mapper = MAPPER.get();
      ObjectNode jsResult = mapper.createObjectNode();
      ArrayNode jsOffers = mapper.createArrayNode();
      for (OfferData offer : offers) {
        ObjectNode jsOffer = mapper.createObjectNode();
        jsOffer.put("id", offer.id);
        jsOffer.put("title", offer.title);
        jsOffer.put("payment", offer.payment);
        jsOffer.put("type", offer.type);
        jsOffer.put("description", offer.description);
        jsOffer.put("image", offer.image);
        jsOffer.put("videoUrl", offer.videoUrl);
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
