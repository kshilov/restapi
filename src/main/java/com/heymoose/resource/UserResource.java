package com.heymoose.resource;

import com.heymoose.domain.AdminAccountAccessor;
import com.heymoose.domain.affiliate.MessengerType;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.UserRepository.Ordering;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.conflict;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.resource.xml.XmlUsers;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;
import java.math.BigDecimal;
import java.net.URI;
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

@Path("users")
@Singleton
public class UserResource {

  private final UserRepository users;
  private final AdminAccountAccessor adminAccountAccessor;
  private final Accounting accounting;

  @Inject
  public UserResource(UserRepository users, AdminAccountAccessor adminAccountAccessor, Accounting accounting) {
    this.users = users;
    this.adminAccountAccessor = adminAccountAccessor;
    this.accounting = accounting;
  }
  
  @GET
  @Path("list")
  @Transactional
  public XmlUsers list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                       @QueryParam("asc") @DefaultValue("false") boolean asc,
                       @QueryParam("full") @DefaultValue("false") boolean full,
                       @QueryParam("role") Role role) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Mappers.toXmlUsers(users.list(offset, limit, ord, asc, role), d);
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
                           @FormParam("password_hash") String passwordHash,
                           @FormParam("organization") String organization,
                           @FormParam("phone") String phone,
                           @FormParam("referrer") Long referrerId) {
    checkNotNull(email, passwordHash);
    User existing = users.byEmail(email);
    if (existing != null)
      return Response.status(409).build();
    User newUser = new User(email, passwordHash, organization, phone, referrerId);
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
    XmlUser xmlUser = Mappers.toXmlUser(user, d);
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
    return Response.ok(Mappers.toXmlUser(user, d)).build();
  }

  @POST
  @Path("{id}/roles")
  @Transactional
  public void addRole(@PathParam("id") Long id, @FormParam("role") Role role) {
    checkNotNull(id, role);
    User user = existing(id);
    user.addRole(role);
    if (role == Role.ADMIN) {
      Account account = adminAccountAccessor.getAdminAccount();
      if (account == null)
        throw new IllegalStateException();
    }
  }

  @PUT
  @Path("{id}/customer-account")
  @Transactional
  public void addToCustomerAccount(@PathParam("id") Long id, @FormParam("amount") String amount) {
    checkNotNull(id, amount);
    User user = existing(id);
    if (!user.isAdvertiser())
      throw conflict();
    AccountingEntry entry = new AccountingEntry(user.advertiserAccount(), new BigDecimal(amount));
    accounting.applyEntry(entry);
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void update(@Context HttpContext context, @PathParam("id") long id) {
    User user = existing(id);
    Form params = context.getRequest().getEntity(Form.class);
    if (params.containsKey("email"))
      user.setEmail(params.getFirst("email"));
    if (params.containsKey("password_hash"))
      user.changePasswordHash(params.getFirst("password_hash"));
    if (params.containsKey("first_name"))
      user.setFirstName(params.getFirst("first_name"));
    if (params.containsKey("last_name"))
      user.setLastName(params.getFirst("last_name"));
    if (params.containsKey("organization"))
      user.setOrganization(nullableParam(params.getFirst("organization")));
    if (params.containsKey("phone"))
      user.setPhone(nullableParam(params.getFirst("phone")));
    if (params.containsKey("source_url")) {
      String sourceUrlParam = params.getFirst("source_url");
      user.setSourceUrl(!isNull(sourceUrlParam) ? URI.create(sourceUrlParam) : null);
    }
    if (params.containsKey("messenger_type")) {
      String messengerTypeParam = params.getFirst("messenger_type");
      String messengerUidParam = params.containsKey("messenger_uid") ? params.getFirst("messenger_uid") : "";
      if (!isNull(messengerTypeParam)) {
        if (isNull(messengerUidParam))
          throw new WebApplicationException(400);
        user.setMessenger(Enum.valueOf(MessengerType.class, messengerTypeParam), messengerUidParam);
      }
      else
        user.setMessenger(null, null);
    }
    if (params.containsKey("wmr"))
      user.setWmr(params.getFirst("wmr"));
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
  public void block(@PathParam("id") long id, @FormParam("reason") String reason) {
    User user = existing(id);
    user.block(reason);
  }
  
  @DELETE
  @Path("{id}/blocked")
  @Transactional
  public void unblock(@PathParam("id") long id) {
    User user = existing(id);
    user.unblock();
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
