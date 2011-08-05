package com.heymoose.rest.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.question.QuestionBase;
import com.heymoose.rest.resource.xml.Mappers;
import com.heymoose.rest.resource.xml.XmlApp;
import org.hibernate.Session;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("app")
public class AppResource {

  private final Provider<Session> sessionProvider;

  @Inject
  public AppResource(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response put(@PathParam("id") int id, XmlApp xmlApp) {
    if (!xmlApp.appId.equals(id))
      return Response.status(Response.Status.BAD_REQUEST).build();
    App app = (App) hiber().get(App.class, id);
    if (app == null) {
      app = new App(xmlApp.appId, xmlApp.secret);
      hiber().save(app);
      return Response.status(Response.Status.CREATED).build();
    }
    app.refreshSecret(xmlApp.secret);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") int id) {
    App app = (App) hiber().get(App.class, id);
    if (app == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    return Response.ok(Mappers.toXmlApp(app)).build();
  }
}
