package com.heymoose.events;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.Action;
import java.math.BigDecimal;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class ActionApproved implements Event {

  private final String callback;
  private final String extId;
  private final Long offerId;
  private final BigDecimal amount;

  public ActionApproved(Action action) {
    checkArgument(action.done());
    callback = action.app().callback().toString();
    extId = action.performer().extId();
    offerId = action.offer().id();
    amount = action.app().calcRevenue(action.offer().order().cpa());
  }

  @Override
  public ObjectNode toJson() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    json.put("callback", callback);
    json.put("offerId", offerId);
    json.put("extId", extId);
    json.put("amount", amount.setScale(2, BigDecimal.ROUND_HALF_EVEN));
    return json;
  }
}
