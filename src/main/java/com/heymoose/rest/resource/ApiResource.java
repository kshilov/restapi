package com.heymoose.rest.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.resource.xml.XmlProfile;
import com.heymoose.rest.resource.xml.XmlProfiles;
import org.hibernate.Session;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("api")
@Singleton
public class ApiResource {

  private final Provider<Session> sessionProvider;

  private Session hiber() {
    return sessionProvider.get();
  }

  @Inject
  public ApiResource(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @PUT
  @Path("profiles")
  @Transactional
  public Response putUserProfiles(@QueryParam("app") String appId, XmlProfiles profiles) {
    App app = (App) hiber().get(App.class, appId);
    if (app == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    for (XmlProfile xmlProfile : profiles.profiles) {
      String extId = xmlProfile.extId;
      // TODO: optimize
      UserProfile profile = profileBy(app, extId);
      if (profile == null) {
        profile = new UserProfile(extId, app);
        hiber().save(profile);
      }
    }
    return Response.ok().build();
  }

  @GET
  @Path("questions")
  @Transactional
  public Response getQuestions(@QueryParam("app") String appId, @QueryParam("extId") String extId) {
    App app = (App) hiber().get(App.class, appId);
    if (app == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    UserProfile profile = profileBy(app, extId);
    if (profile == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    // TODO: use targeting
    return null;
  }

  @Transactional
  private UserProfile profileBy(App app, String extId) {
    return (UserProfile) hiber()
              .createQuery("from UserProfile where app = :appId and extId = :extId")
              .setParameter("app", app)
              .setParameter("extId", extId)
              .uniqueResult();
  }
}
