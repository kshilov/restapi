package com.heymoose.resource;

import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.resource.xml.Mappers;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("users")
@Singleton
public class UserResource {

  private final UserRepository users;

  @Inject
  public UserResource(UserRepository users) {
    this.users = users;
  }

  @POST
  public Response register(@FormParam("email") String email, @FormParam("password") String password, @FormParam("nickname") String nickname) {
    User existing = users.byEmail(email);
    if (existing != null)
      return Response.status(400).build();
    User newUser = new User();
    newUser.email = email;
    newUser.nickname = nickname;
    newUser.passwordHash = DigestUtils.md5Hex(password);
    users.put(newUser);
    return Response.created(URI.create(Long.toString(newUser.id))).build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") long id,
                      @QueryParam("full") @DefaultValue("true") boolean full) {
    User user = users.get(id);
    if (user == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlUser(user, full)).build();
  }

  @GET
  public Response getByEmail(@QueryParam("email") String email,
                             @QueryParam("full") @DefaultValue("false") boolean full) {
    User user = users.byEmail(email);
    if (user == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlUser(user, full)).build();
  }
}
