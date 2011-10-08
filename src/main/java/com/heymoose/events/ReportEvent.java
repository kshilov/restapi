package com.heymoose.events;

import com.heymoose.domain.Mlm;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class ReportEvent implements Event {

  private final Mlm.Report report;

  public ReportEvent(Mlm.Report report) {
    this.report = report;
  }

  @Override
  public ObjectNode toJson() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    json.put("appId", report.appId);
    json.put("fromTime", report.fromTime.toString());
    json.put("toTime", report.toTime.toString());
    ArrayNode jsonItems = mapper.createArrayNode();
    for (Mlm.ReportItem item : report.items) {
      ObjectNode jsonItem = mapper.createObjectNode();
      json.put("extId", item.extId);
      json.put("passiveRevenue", item.passiveRevenue);
      jsonItems.add(jsonItem);
    }
    json.put("items", jsonItems);
    return json;
  }
}
