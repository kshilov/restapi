package com.heymoose.security;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class Signer {
  
  private Signer() {}

  public static String sign(long appId, String secret) {
    return md5Hex(appId + secret);
  }

  public static String sign(Map<String, String> params, String secret) {
    checkArgument(!params.isEmpty());
    checkArgument(!isNullOrEmpty(secret));
    StringBuilder sb = new StringBuilder();
    SortedMap<String, String> sortedParams = new TreeMap<String, String>(params);
    for (Map.Entry<String, String> ent : sortedParams.entrySet()) {
      sb.append(ent.getKey());
      sb.append("=");
      sb.append(ent.getValue());
    }
    sb.append(secret);
    return md5Hex(sb.toString());
  }
}
