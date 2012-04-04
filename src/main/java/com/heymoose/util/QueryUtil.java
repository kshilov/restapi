package com.heymoose.util;

import com.google.common.collect.Lists;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class QueryUtil {
  public static URI appendQueryParam(URI uri, String name, Object value) {
    List<NameValuePair> params = Lists.newArrayList(URLEncodedUtils.parse(uri, "UTF-8"));
    params.add(new NameValuePair(name, value.toString()));
    try {
      return URIUtils.createURI(
          uri.getScheme(),
          uri.getHost(),
          uri.getPort(),
          uri.getPath(),
          URLEncodedUtils.format(params, "UTF-8"),
          uri.getFragment()
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}