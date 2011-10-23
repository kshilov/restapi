package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.Offer;
import com.heymoose.security.Signer;
import com.heymoose.util.jtpl.Template;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;

import static com.heymoose.domain.Compensation.subtractCompensation;
import static org.apache.commons.lang.StringUtils.isBlank;

public class HtmlOfferTemplate implements OfferTemplate {

  private final String offerTpl;

  public HtmlOfferTemplate() {
    StringWriter sw = new StringWriter();
    try {
      InputStream is = getClass().getResourceAsStream("/offer.jtpl");
      IOUtils.copy(is, sw);
      is.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.offerTpl = sw.toString();
  }

  @Override
  public String render(Iterable<Offer> offers, App app, String extId, BigDecimal compensation) {
    Template out = new Template(offerTpl);
    for (Offer offer : offers) {
      out.assign("TITLE", offer.title());
      out.assign("DESCRIPTION", offer.description());
      out.assign("BODY", offer.body());
      out.assign("IMG", offer.imageBase64());
      out.assign("APP", Long.toString(app.id()));
      out.assign("SIG", Signer.sign(app.id(), app.secret()));
      out.assign("OFFER", Long.toString(offer.id()));
      BigDecimal payment = subtractCompensation(offer.order().cpa(), compensation);
      out.assign("PAYMENT", payment.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
      if (!isBlank(extId))
        out.assign("EXT", extId);
      else
        out.assign("EXT", "");
      out.parse("main.offer");
    }
    out.parse("main");
    return out.out();
  }
}
