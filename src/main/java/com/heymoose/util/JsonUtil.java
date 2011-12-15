package com.heymoose.util;

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class JsonUtil {

  private JsonUtil() {}

  public static String toString(ObjectNode json) {
    try {
      return MAPPER.get().writeValueAsString(json);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private final static ThreadLocal<ObjectMapper> MAPPER = new ThreadLocal<ObjectMapper>() {
    @Override
    protected ObjectMapper initialValue() {
      return new ObjectMapper();
    }
  };
}
