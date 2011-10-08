package com.heymoose.events;

import com.heymoose.domain.Action;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

public class ActionApproved implements Event {

  private final Action action;
  private final BigDecimal compensation;

  public ActionApproved(Action action, BigDecimal compensation) {
    checkArgument(action.done());
    checkArgument(compensation.signum() == 1);
    checkArgument(compensation.compareTo(new BigDecimal("1.0")) == -1);
    this.compensation = compensation;
    this.action = action;
  }

  @Override
  public ObjectNode toJson() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    String callback = action.performer().app().callback().toString();
    String extId = action.performer().extId();
    BigDecimal amount = action.reservation().diff().negate().multiply(compensation);
    json.put("callback", callback);
    json.put("extId", extId);
    json.put("amount", amount.setScale(2, BigDecimal.ROUND_HALF_EVEN));
    return json;
  }
}
