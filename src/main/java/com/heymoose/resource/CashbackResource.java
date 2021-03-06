package com.heymoose.resource;

import com.google.inject.Inject;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.resource.xml.XmlQueryResult;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.Map;

@Path("/cashbacks")
public class CashbackResource {

  private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter();

  private final Cashbacks cashbacks;

  @Inject
  public CashbackResource(Cashbacks cashbacks) {
    this.cashbacks = cashbacks;
  }

  @GET
  @Produces("application/xml")
  @Transactional
  public String list(@QueryParam("aff_id") Long affId,
                     @QueryParam("offset") int offset,
                     @QueryParam("limit") @DefaultValue("20") int limit) {
    if (affId == null) throw new WebApplicationException(400);
    return toXml(cashbacks.list(affId, offset, limit));
  }

  @GET
  @Path("/invites")
  @Produces("application/xml")
  @Transactional
  public String listInvites(@QueryParam("aff_id") Long affId,
                            @QueryParam("offset") int offset,
                            @QueryParam("limit") @DefaultValue("20") int limit) {
    if (affId == null) throw new WebApplicationException(400);
    Pair<QueryResult, Long> result =
        cashbacks.listInvites(affId, offset, limit);
    return new XmlQueryResult(result.fst)
        .addRootAttribute("count", result.snd)
        .setRoot("invites")
        .setElement("invite")
        .toString();
  }

  private String toXml(Pair<QueryResult, Long> result) {
    Element root = new Element("cashbacks")
        .setAttribute("count", result.snd.toString());
    for (Map<String, Object> map : result.fst) {
      TypedMap entry = TypedMap.wrap(map);
      Element cashbackXml = new Element("cashback");
      cashbackXml.addContent(element("target", entry.getString("target_id")));
      cashbackXml.addContent(element("date", entry.getDateTime("date")));
      Element offerXml = new Element("offer")
          .setAttribute("id", entry.getString("offer_id"))
          .addContent(element("name", entry.getString("offer_name")));
      cashbackXml.addContent(offerXml);
      cashbackXml.addContent(
          element("affiliate-revenue", entry.getString("affiliate_revenue")));
      root.addContent(cashbackXml);
    }
    return XML_OUTPUTTER.outputString(new Document(root));
  }

  private Element element(String name, Object text) {
    return new Element(name).setText(text.toString());
  }
}
