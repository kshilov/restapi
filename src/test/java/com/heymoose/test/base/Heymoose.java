package com.heymoose.test.base;

import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlAction;
import com.heymoose.resource.xml.XmlActions;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlOrder;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.security.Signer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Ignore
public class Heymoose {

  private final WebResource client;

  public Heymoose(WebResource client) {
    this.client = client;
  }

  public long registerUser(String email, String nickname, String passwordHash) {
    Form form = new Form();
    form.add("email", email);
    form.add("nickname", nickname);
    form.add("passwordHash", passwordHash);
    ClientResponse response = client.path("users").post(ClientResponse.class, form);
    return Long.valueOf(response.getLocation().getPath().replaceFirst("/users/", ""));
  }

  public XmlUser getUser(long userId) {
    return client.path("users").path(Long.toString(userId)).get(XmlUser.class);
  }

  public XmlUser getUserByEmail(String email) {
    return client.path("users").queryParam("email", email).get(XmlUser.class);
  }

  public void addRoleToUser(long userId, Role role) {
    Form form = new Form();
    form.add("role", role.toString());
    client.path("users").path(Long.toString(userId)).put(form);
  }

  public void addToCustomerAccount(long userId, double amount) {
    Form form = new Form();
    form.add("amount", Double.toString(amount));
    client.path("users").path(Long.toString(userId)).path("customer-account").put(form);
  }

  public void createApp(long userId, String url, String callback) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("url", url);
    form.add("callback", callback);
    client.path("apps").post(form);
  }

  public XmlApp getApp(long appId) {
    return client.path("apps").path(Long.toString(appId)).get(XmlApp.class);
  }

  public void regenerateSecret(long appId) {
    client.path("apps").path(Long.toString(appId)).put();
  }

  public void deleteApp(long appId) {
    client.path("apps").path(Long.toString(appId)).delete();
  }

  public long createOrder(long userId,
                          String title,
                          String description,
                          String body,
                          String image,
                          double balance,
                          double cpa,
                          boolean allowNegativeBalance) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("description", description);
    form.add("body", body);
    form.add("image", image);
    form.add("balance", balance);
    form.add("cpa", cpa);
    form.add("allowNegativeBalance", allowNegativeBalance);
    return Long.valueOf(client.path("orders").post(String.class, form));
  }

  public XmlOrder getOrder(long orderId) {
    return client.path("orders").path(Long.toString(orderId)).get(XmlOrder.class);
  }

  public void approveOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).put();
  }

  public void disableOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).delete();
  }

  public XmlOffers getAvailableOffers(XmlApp app, String extId) {
    Map<String, String> params = newHashMap();
    params.put("method", "getOffers");
    params.put("app_id", Long.toString(app.id));
    params.put("uid", extId);
    params.put("format", "JSON");
    String sig = Signer.sign(params, app.secret);
    String response = client.path("api")
        .queryParam("method", "getOffers")
        .queryParam("app_id", Long.toString(app.id))
        .queryParam("uid", extId)
        .queryParam("format", "JSON")
        .queryParam("sig", sig)
        .get(String.class);
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsResult;
    try {
      jsResult = (ObjectNode) mapper.readTree(response);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    ArrayNode jsOffers = (ArrayNode) jsResult.get("result");
    XmlOffers xmlOffers = new XmlOffers();
    for (JsonNode node : jsOffers) {
      ObjectNode jsOffer = (ObjectNode) node;
      XmlOffer xmlOffer = new XmlOffer();
      xmlOffer.id = jsOffer.get("id").getLongValue();
      xmlOffer.title = jsOffer.get("title").getTextValue();
      xmlOffers.offers.add(xmlOffer);
    }
    return xmlOffers;
  }

  public URI doOffer(XmlApp app, long offerId, String extId, Platform platform) {
    Map<String, String> params = newHashMap();
    params.put("method", "doOffer");
    params.put("app_id", Long.toString(app.id));
    params.put("offer_id", Long.toString(offerId));
    params.put("uid", extId);
    params.put("platform", platform.name());
    String sig = Signer.sign(params, app.secret);
    ClientResponse response = client.path("api")
        .queryParam("method", "doOffer")
        .queryParam("app_id", Long.toString(app.id))
        .queryParam("offer_id", Long.toString(offerId))
        .queryParam("uid", extId)
        .queryParam("platform", platform.name())
        .queryParam("sig", sig)
        .get(ClientResponse.class);
    if (response.getStatus() != 302)
      throw new UniformInterfaceException(response);
    return response.getLocation();
  }

  public XmlActions getActions(int offset, int limit) {
    return client.path("actions").queryParam("offset", Integer.toString(offset)).queryParam("limit", Integer.toString(limit)).get(XmlActions.class);
  }

  public void approveAction(XmlUser user, long actionId) {
    Map<String, String> params = newHashMap();
    params.put("method", "approveAction");
    params.put("customer_id", Long.toString(user.id));
    params.put("action_id", Long.toString(actionId));
    String sig = Signer.sign(params, user.customerSecret);
    ClientResponse response = client.path("api")
        .queryParam("method", "approveAction")
        .queryParam("customer_id", Long.toString(user.id))
        .queryParam("action_id", Long.toString(actionId))
        .queryParam("sig", sig)
        .get(ClientResponse.class);
    if (response.getStatus() != 200)
      throw new UniformInterfaceException(response);
  }

  public void deleteAction(long actionId) {
    client.path("actions").path(Long.toString(actionId)).delete();
  }
}
