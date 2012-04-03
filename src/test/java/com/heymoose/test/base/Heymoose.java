package com.heymoose.test.base;

import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.resource.xml.XmlCategories;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlOfferGrant;
import com.heymoose.resource.xml.XmlUser;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.representation.Form;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.Set;
import org.junit.Ignore;

@Ignore
public class Heymoose {

  private final WebResource client;

  public Heymoose(WebResource client) {
    client.addFilter(new ClientFilter() {
      @Override
      public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        cr.getHeaders().put("X-Real-Ip", asList((Object)"127.0.0.1"));
        return getNext().handle(cr);
      }
    });
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

  public XmlCategories getCategories() {
    return client.path("categories").get(XmlCategories.class);
  }

  public void confirmUser(long userId) {
    client.path("users").path(Long.toString(userId)).path("confirmed").put();
  }

  public Long createOffer(long advertiserId, PayMethod payMethod, CpaPolicy cpaPolicy, double cost,
                          double balance, String name, String descr, String logoFileName, URI uri,
                          String title, boolean allowNegativeBalance, boolean autoApprove,
                          boolean reentrant, Set<Region> regions, Set<Long> categories) {
    Form form = new Form();
    form.add("advertiser_id", advertiserId);
    form.add("pay_method", payMethod);
    form.add("cpa_policy", cpaPolicy);
    form.add("cost", cost);
    form.add("balance", balance);
    form.add("name", name);
    form.add("description", descr);
    form.add("logo_filename", logoFileName);
    form.add("url", uri);
    form.add("title", title);
    form.add("allow_negative_balance", allowNegativeBalance);
    form.add("auto_approve", autoApprove);
    form.add("reentrant", reentrant);
    for (Region region : regions)
      form.add("regions", region);
    for (long categoryId : categories)
      form.add("categories", categoryId);
    return Long.valueOf(client.path("offers").post(String.class, form));
  }

  public XmlOffer getOffer(long offerId) {
    return client.path("offers").path(Long.toString(offerId)).get(XmlOffer.class);
  }

  public void track(long offerId, long affId) {
    client.path("api")
        .queryParam("method", "track")
        .queryParam("offer_id", Long.toString(offerId))
        .queryParam("aff_id", Long.toString(affId))
        .get(String.class);
  }

  public URI click(long offerId, long affId) {
    ClientResponse response = client.path("api")
        .queryParam("method", "click")
        .queryParam("offer_id", Long.toString(offerId))
        .queryParam("aff_id", Long.toString(affId))
        .get(ClientResponse.class);
    return response.getLocation();
  }

  public void approveOffer(long offerId) {
    client.path("offers").path(Long.toString(offerId)).path("blocked").delete();
  }

  public long createGrant(long offerId, long affId, String message) {
    Form form = new Form();
    form.add("offer_id", offerId);
    form.add("aff_id", affId);
    form.add("message", message);
    return Long.valueOf(client.path("grants").post(String.class, form));
  }

  // as admin
  public void unblockGrant(long grantId) {
    client.path("grants").path(Long.toString(grantId)).path("blocked").delete();
  }

  // as adv
  public void approveGrant(long grantId) {
    client.path("grants").path(Long.toString(grantId)).path("approved").put();
  }

  public XmlOfferGrant getGrant(long grantId) {
    return client.path("grants").path(Long.toString(grantId)).get(XmlOfferGrant.class);
  }
}
