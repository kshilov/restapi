package com.heymoose.test.base;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlUser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.junit.Ignore;

@Ignore
public class Heymoose {

  private final WebResource client;

  public Heymoose(WebResource client) {
    this.client = client;
  }

  public long registerUser(String email, String passwordHash, String firstName, String lastName, String phone) {
    Form form = new Form();
    form.add("email", email);
    form.add("passwordHash", passwordHash);
    form.add("firstName", firstName);
    form.add("lastName", lastName);
    form.add("phone", phone);
    ClientResponse response = client.path("users").post(ClientResponse.class, form);
    if (response.getStatus() >= 300)
      throw new UniformInterfaceException(response);
    return Long.valueOf(response.getLocation().getPath().replaceFirst("/users/", ""));
  }

  public void updateUser(long userId, String passwordHash) {
    Form form = new Form();
    form.add("passwordHash", passwordHash);
    client.path("users").path(Long.toString(userId)).put(form);
  }

  public XmlUser getUser(long userId) {
    return client.path("users").path(Long.toString(userId)).queryParam("full", "true").get(XmlUser.class);
  }

  public XmlUser getUserByEmail(String email) {
    return client.path("users").queryParam("email", email).get(XmlUser.class);
  }

  public void addRoleToUser(long userId, Role role) {
    Form form = new Form();
    form.add("role", role.toString());
    client.path("users").path(Long.toString(userId)).path("roles").post(form);
  }

  public void addToCustomerAccount(long userId, double amount) {
    Form form = new Form();
    form.add("amount", Double.toString(amount));
    client.path("users").path(Long.toString(userId)).path("customer-account").put(form);
  }

  public void regenerateSecret(long appId) {
    client.path("apps").path(Long.toString(appId)).path("secret").put();
  }

  public void deleteApp(long appId) {
    client.path("apps").path(Long.toString(appId)).delete();
  }

  public long createRegularOrder(long userId,
                                 String title,
                                 String description,
                                 String url,
                                 String image,
                                 double balance,
                                 double cpa,
                                 boolean allowNegativeBalance) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("description", description);
    form.add("url", url);
    form.add("image", image);
    form.add("balance", balance);
    form.add("cpa", cpa);
    form.add("allowNegativeBalance", allowNegativeBalance);
    form.add("type", "REGULAR");
    return Long.valueOf(client.path("orders").post(String.class, form));
  }

  public long createVideoOrder(long userId,
                                 String title,
                                 String videoUrl,
                                 String url,
                                 double balance,
                                 double cpa,
                                 boolean allowNegativeBalance) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("videoUrl", videoUrl);
    form.add("url", url);
    form.add("balance", balance);
    form.add("cpa", cpa);
    form.add("allowNegativeBalance", allowNegativeBalance);
    form.add("type", "VIDEO");
    return Long.valueOf(client.path("orders").post(String.class, form));
  }

  public long createBannerOrder(long userId,
                                 String title,
                                 String url,
                                 String image,
                                 String bannerMimeType,
                                 long bannerSize,
                                 double balance,
                                 double cpa,
                                 boolean allowNegativeBalance) {
    Form form = new Form();
    form.add("userId", userId);
    form.add("title", title);
    form.add("bannerMimeType", bannerMimeType);
    form.add("bannerSize", bannerSize);
    form.add("url", url);
    form.add("image", image);
    form.add("balance", balance);
    form.add("cpa", cpa);
    form.add("allowNegativeBalance", allowNegativeBalance);
    form.add("type", "BANNER");
    return Long.valueOf(client.path("orders").post(String.class, form));
  }

  public long bannerSize(int width, int height) {
    Form form = new Form();
    form.add("width", width);
    form.add("height", height);
    return Long.valueOf(client.path("banner-sizes").post(String.class, form));
  }

  public void approveOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).path("enabled").put();
  }

  public void disableOrder(long orderId) {
    client.path("orders").path(Long.toString(orderId)).path("enabled").delete();
  }

  public void deleteAction(long actionId) {
    client.path("actions").path(Long.toString(actionId)).delete();
  }
}
