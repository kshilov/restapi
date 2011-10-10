package com.heymoose.test.base;

import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlAction;
import com.heymoose.resource.xml.XmlActions;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlOrder;
import com.heymoose.resource.xml.XmlUser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Ignore;

import java.net.URI;

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

  public void createApp(long userId, String callback) {
    Form form = new Form();
    form.add("userId", userId);
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
                          double cpa) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("description", description);
    form.add("body", body);
    form.add("image", image);
    form.add("balance", balance);
    form.add("cpa", cpa);
    return Long.valueOf(client.path("orders").post(String.class, form));
  }

  public XmlOrder getOrder(long orderId) {
    return client.path("orders").path(Long.toString(orderId)).get(XmlOrder.class);
  }

  public void approveOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).put();
  }

  public void deleteOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).delete();
  }

  public XmlOffers getAvailableOffers(XmlApp app, String extId) {
    String sig = DigestUtils.md5Hex(app.id + app.secret);
    return client
        .path("offers/internal/available")
        .queryParam("app", Long.toString(app.id))
        .queryParam("sig", sig)
        .queryParam("extId", extId)
        .get(XmlOffers.class);
  }

  public URI doOffer(XmlApp app, long offerId, String extId, Platform platform) {
    String sig = DigestUtils.md5Hex(app.id + app.secret);
    Form form = new Form();
    form.add("extId", extId);
    form.add("platform", platform);
    ClientResponse response = client
        .path("offers")
        .path(Long.toString(offerId))
        .queryParam("app", Long.toString(app.id))
        .queryParam("sig", sig)
        .post(ClientResponse.class, form);
    if (response.getStatus() != 302)
      throw new UniformInterfaceException(response);
    return response.getLocation();
  }

  public XmlActions getActions(int offset, int limit) {
    return client.path("actions").queryParam("offset", Integer.toString(offset)).queryParam("limit", Integer.toString(limit)).get(XmlActions.class);
  }

  public void approveAction(long actionId) {
    client.path("actions").path(Long.toString(actionId)).put();
  }

  public void deleteAction(long actionId) {
    client.path("actions").path(Long.toString(actionId)).delete();
  }
}
