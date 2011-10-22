package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;

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
    return Response.ok(Mappers.toXmlUser(user, full)).build();
  }

  @GET
  @Transactional
  public Response getByEmail(@QueryParam("email") String email,
                             @QueryParam("full") @DefaultValue("false") boolean full) {
    checkNotNull(email);
    User user = users.byEmail(email);
    if (user == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlUser(user, full)).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response addRole(@PathParam("id") Long id, @FormParam("role") Role role) {
    checkNotNull(id, role);
    User user = existing(id);
    user.addRole(role);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}/customer-account")
  @Transactional
  public Response addToCustomerAccount(@PathParam("id") Long id, @FormParam("amount") String amount) {
    checkNotNull(id, amount);
    User user = existing(id);
    if (!user.isCustomer())
      return Response.status(Response.Status.CONFLICT).build();
    accounts.lock(user.customerAccount());
    user.customerAccount().addToBalance(new BigDecimal(amount), "Adding to balance");
    return Response.ok().build();
  }

  private User existing(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }
}
