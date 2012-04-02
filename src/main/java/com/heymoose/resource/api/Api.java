package com.heymoose.resource.api;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.UserRepository;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URIUtils;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Api {

  private final static Logger LOGGER = LoggerFactory.getLogger(Api.class);

  private final UserRepository users;

  @Inject
  public Api(UserRepository users) {
    this.users = users;
  }

  public static URI appendQueryParam(URI uri, String name, Object value) {
    List<NameValuePair> params = newArrayList(URLEncodedUtils.parse(uri, "UTF-8"));
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
