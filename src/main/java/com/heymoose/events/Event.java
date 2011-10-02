package com.heymoose.events;

import org.codehaus.jackson.node.ObjectNode;

public interface Event {
  ObjectNode toJson();
}
