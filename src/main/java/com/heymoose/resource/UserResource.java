package com.heymoose.resource;

import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.MessengerType;
import com.heymoose.domain.Role;
import com.heymoose.domain.TxType;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.Withdraw;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.conflict;
import static com.heymoose.resource.Exceptions.notFound;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;

import com.heymoose.resource.xml.XmlUser;
import com.heymoose.resource.xml.XmlWithdraws;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("users")
@Singleton
public class UserResource {

  private final UserRepository users;
  private final Accounts accounts;

  @Inject
  public UserResource(UserRepository users, Accounts accounts) {
    this.users = users;
    this.accounts = accounts;
  }
  
  @GET
  @Path("list")
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full,
                       @QueryParam("role") String role) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    Role r = null;
    try { r = Role.valueOf(role.toUpperCase()); } catch (Exception e) { }
    return Response.ok(Mappers.toXmlUsers(accounts, users.list(offset, limit, r), d)).build();
  }
  
  @GET
  @Path("list/count")
  @Transactional
  public Response count(@QueryParam("role") String role) {
    Role r = null;
    try { r = Role.valueOf(role.toUpperCase()); } catch (Exception e) { }
    return Response.ok(Mappers.toXmlCount(users.count(r))).build();
  }

  @POST
  @Transactional
  public Response register(@FormParam("email") String email,
                           @FormParam("passwordHash") String passwordHash,
                           @FormParam("firstName") String firstName,
                           @FormParam("lastName") String lastName,
                           @FormParam("organization") String organization,
                           @FormParam("phone") String phone,
                           @FormParam("sourceUrl") String sourceUrl,
                           @FormParam("messengerType") MessengerType messengerType,
                           @FormParam("messengerUid") String messengerUid,
                           @FormParam("referrer") Long referrerId) {
    checkNotNull(email, passwordHash, firstName, lastName);
    User existing = users.byEmail(email);
    if (existing != null)
      return Response.status(409).build();
    URI uriSourceUrl = sourceUrl != null ? URI.create(sourceUrl) : null;
    User newUser = new User(email, passwordHash, firstName, lastName, organization,
        phone, uriSourceUrl, messengerType, messengerUid, referrerId);
    users.put(newUser);
    return Response.created(URI.create(Long.toString(newUser.id()))).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") Long id,
                      @QueryParam("full") @DefaultValue("false") boolean full) {
    checkNotNull(id);
    User user = existing(id);
    Details d = full ? Details.WITH_RELATED_LISTS : Details.ONLY_ENTITY;
    XmlUser xmlUser = Mappers.toXmlUser(accounts, user, d);
    if (user.isCustomer()) {
      BigDecimal revenue = new BigDecimal(0);
      for (AccountTx tx : user.customerAccount().transactions())
        if ("MLM".equals(tx.description()))
          revenue = revenue.add(tx.diff());
      xmlUser.revenue = revenue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
    }
    xmlUser.referrer = user.referrerId();
    Iterable<User> referrals = users.referrals(id);
    for (User r : referrals)
      xmlUser.referrals.add(r.email());
    return Response.ok(xmlUser).build();
  }

  @GET
  @Transactional
  public Response getByEmail(@QueryParam("email") String email,
                             @QueryParam("full") @DefaultValue("false") boolean full) {
    checkNotNull(email);
    User user = users.byEmail(email);
    if (user == null)
      return Response.status(404).build();
    Details d = full ? Details.WITH_RELATED_LISTS : Details.ONLY_ENTITY;
    return Response.ok(Mappers.toXmlUser(accounts, user, d)).build();
  }

  @POST
  @Path("{id}/roles")
  @Transactional
  public void addRole(@PathParam("id") Long id, @FormParam("role") Role role) {
    checkNotNull(id, role);
    User user = existing(id);
    user.addRole(role);
  }

  @PUT
  @Path("{id}/customer-account")
  @Transactional
  public void addToCustomerAccount(@PathParam("id") Long id, @FormParam("amount") String amount) {
    checkNotNull(id, amount);
    User user = existing(id);
    if (!user.isCustomer())
      throw conflict();
    accounts.lock(user.customerAccount());
    accounts.addToBalance(user.customerAccount(), new BigDecimal(amount), "Adding to balance",
        TxType.REPLENISHMENT_ADMIN);
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void update(@Context HttpContext context, @PathParam("id") long id) {
    User user = existing(id);
    Form params = context.getRequest().getEntity(Form.class);
    if (params.containsKey("email"))
      user.setEmail(params.getFirst("email"));
    if (params.containsKey("passwordHash"))
      user.changePasswordHash(params.getFirst("passwordHash"));
    if (params.containsKey("firstName"))
      user.setFirstName(params.getFirst("firstName"));
    if (params.containsKey("lastName"))
      user.setLastName(params.getFirst("lastName"));
    if (params.containsKey("organization"))
      user.setOrganization(nullableParam(params.getFirst("organization")));
    if (params.containsKey("phone"))
      user.setPhone(nullableParam(params.getFirst("phone")));
    if (params.containsKey("sourceUrl")) {
      String sourceUrlParam = params.getFirst("sourceUrl");
      user.setSourceUrl(!isNull(sourceUrlParam) ? URI.create(sourceUrlParam) : null);
    }
    if (params.containsKey("messengerType")) {
      String messengerTypeParam = params.getFirst("messengerType");
      String messengerUidParam = params.containsKey("messengerUid") ? params.getFirst("messengerUid") : "";
      if (!isNull(messengerTypeParam)) {
        if (isNull(messengerUidParam))
          throw new WebApplicationException(400);
        user.setMessenger(Enum.valueOf(MessengerType.class, messengerTypeParam), messengerUidParam);
      }
      else
        user.setMessenger(null, null);
    }
    if (params.containsKey("confirmed"))
      user.setConfirmed(Boolean.valueOf(params.getFirst("confirmed")));
    if (params.containsKey("blocked"))
      user.setBlocked(Boolean.valueOf(params.getFirst("blocked")));
  }
  
  @PUT
  @Path("{id}/confirmed")
  @Transactional
  public void confirm(@PathParam("id") long id) {
    User user = existing(id);
    user.setConfirmed(true);
  }
  
  @PUT
  @Path("{id}/blocked")
  @Transactional
  public void block(@PathParam("id") long id) {
    User user = existing(id);
    user.setBlocked(true);
  }
  
  @DELETE
  @Path("{id}/blocked")
  @Transactional
  public void unblock(@PathParam("id") long id) {
    User user = existing(id);
    user.setBlocked(false);
  }
  
  @PUT
  @Path("{id}/email")
  @Transactional
  public void changeEmail(@PathParam("id") long id,
                          @FormParam("email") String email) {
    checkNotNull(email);
    User user = existing(id);
    user.changeEmail(email);
  }

  @POST
  @Path("{id}/developer-account/withdraws")
  @Transactional
  public String createWithdraw(@PathParam("id") long id, @FormParam("amount") String _amount) {
    checkNotNull(_amount);
    BigDecimal amount = new BigDecimal(_amount);
    User user = existing(id);
    if (!user.isDeveloper())
      throw conflict();
    Withdraw withdraw = accounts.withdraw(user.developerAccount(), amount);
    return Long.toString(withdraw.id());
  }

  @GET
  @Transactional
  @Path("{id}/developer-account/withdraws")
  public XmlWithdraws developerWithdraws(@PathParam("id") long id) {
    User user = existing(id);
    if (!user.isDeveloper())
      throw conflict();
    List<Withdraw> withdraws = accounts.withdraws(user.developerAccount());
    return Mappers.toXmlWithdraws(user.developerAccount().id(), withdraws);
  }

  @PUT
  @Transactional
  @Path("{id}/developer-account/withdraws/{withdrawId}")
  public void approveDeveloperWithdraw(@PathParam("id") long id, @PathParam("withdrawId") long withdrawId) {
    User user = existing(id);
    if (!user.isDeveloper())
      throw conflict();
    Withdraw withdraw = accounts.withdrawOfAccount(user.developerAccount(), withdrawId);
    if (withdraw == null)
      throw notFound();
    withdraw.approve();
  }

  @DELETE
  @Transactional
  @Path("{id}/developer-account/withdraws/{withdrawId}")
  public void deleteDeveloperWithdraw(@PathParam("id") long id, @PathParam("withdrawId") long withdrawId, @FormParam("comment") String comment) {
    checkNotNull(comment);
    User user = existing(id);
    if (!user.isDeveloper())
      throw conflict();
    Withdraw withdraw = accounts.withdrawOfAccount(user.developerAccount(), withdrawId);
    if (withdraw == null)
      throw notFound();
    accounts.deleteWithdraw(withdraw, comment);
  }

  private User existing(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }
  
  private boolean isNull(String param) {
    String paramLower = param.toLowerCase();
    return paramLower.isEmpty() || paramLower.equals("null") || paramLower.equals("none");
  }
  
  private String nullableParam(String param) {
    if (isNull(param))
      return null;
    return param;
  }
}
