package com.heymoose.events;

import com.heymoose.domain.Action;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.heymoose.domain.Compensation.subtractCompensation;

public class ActionApproved implements Event {

  private final String callback;
  private final String extId;
  private final Long offerId;
  private final BigDecimal amount;

  public ActionApproved(Action action, BigDecimal compensation) {
    checkArgument(action.done());
    checkArgument(compensation.signum() == 1);
    checkArgument(compensation.compareTo(new BigDecimal("1.0")) == -1);
    callback = action.app().callback().toString();
    extId = action.performer().extId();
    offerId = action.offer().id();
    amount = subtractCompensation(action.reservedAmount(), compensation);
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
