package com.heymoose.test.base;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlOrder;
import com.heymoose.resource.xml.XmlUser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.junit.Ignore;

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

  public void createApp(long userId) {
    Form form = new Form();
    form.add("userId", userId);
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

  public void createOrder(long userId,
                          String title,
                          String body,
                          double balance,
                          double cpa) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("body", body);
    form.add("balance", balance);
    form.add("cpa", cpa);
    client.path("orders").post(form);
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
}
