package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.conflict;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
    return Response.ok(Mappers.toXmlUsers(users.list(offset, limit, r), d)).build();
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
                           @FormParam("nickname") String nickname) {
    checkNotNull(email, passwordHash, nickname);
    User existing = users.byEmail(email);
    if (existing != null)
      return Response.status(400).build();
    User newUser = new User(email, nickname, passwordHash);
    users.put(newUser);
    return Response.created(URI.create(Long.toString(newUser.id()))).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") Long id,
                      @QueryParam("full") @DefaultValue("true") boolean full) {
    checkNotNull(id);
    User user = existing(id);
    Details d = full ? Details.WITH_RELATED_LISTS : Details.ONLY_ENTITY;
    return Response.ok(Mappers.toXmlUser(user, d)).build();
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
    user.customerAccount().addToBalance(new BigDecimal(amount), "Adding to balance");
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void update(@PathParam("id") long id,
                     @FormParam("passwordHash") String passwordHash) {
    User user = existing(id);
    if (passwordHash != null)
      user.changePasswordHash(passwordHash);
  }

  private User existing(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }
}
