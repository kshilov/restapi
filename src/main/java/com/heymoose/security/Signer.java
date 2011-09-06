package com.heymoose.security;

import org.apache.commons.codec.digest.DigestUtils;

public class Signer {
  private Signer() {}

  public static String sign(long appId, String secret) {
    return DigestUtils.md5Hex(appId + secret);
  }
}
