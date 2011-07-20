package com.heymoose.rest.resource;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.question.BaseQuestion;
import com.heymoose.rest.domain.question.Questions;
import com.heymoose.rest.domain.security.Secured;
import com.heymoose.rest.resource.xml.Mappers;
import com.heymoose.rest.resource.xml.XmlProfile;
import com.heymoose.rest.resource.xml.XmlProfiles;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Path("api")
@Singleton
@Secured
public class ApiResource {

  private final static Logger log = LoggerFactory.getLogger(ApiResource.class);

  private final Provider<Session> sessionProvider;
  private final int maxShows;
  private final Questions questions;

  private Session hiber() {
    return sessionProvider.get();
  }

  @Inject
  public ApiResource(Provider<Session> sessionProvider, @Named("settings") Properties settings, Questions questions) {
    this.sessionProvider = sessionProvider;
    this.questions = questions;
    this.maxShows = Integer.parseInt(settings.getProperty("max-shows"));
  }

  @PUT
  @Path("profiles")
  @Transactional
  public Response putUserProfiles(@QueryParam("app") int appId, XmlProfiles profiles) {
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
  @SuppressWarnings("unchecked")
  public Response getQuestions(@QueryParam("app") int appId,
                               @QueryParam("count") int count,
                               @QueryParam("extId") String extId) {

    App app = existing((App) hiber().get(App.class, appId));

    List<BaseQuestion> questions = hiber()
              .createQuery("from BaseQuestion where asked <= :maxShows")
              .setParameter("maxShows", maxShows)
              .setMaxResults(count)
              .list();

    // WARNING: may be races
    for (BaseQuestion question : questions)
      this.questions.reserve(question);

    // TODO: check for user answers by extId

    return Response.ok(Mappers.toXmlQuestions(questions)).build();
  }

  @POST
  @Path("profiles")
  @Transactional
  public Response sendProfiles(@QueryParam("app") int appId, XmlProfiles xmlProfiles) {
    App app = existing((App) hiber().get(App.class, appId));

    Set<String> extIds = Sets.newHashSet();
    for (XmlProfile xmlProfile : xmlProfiles.profiles)
      extIds.add(xmlProfile.extId);

    Set<String> existing = existingProfileExtIds(extIds, app);

    for (XmlProfile xmlProfile : xmlProfiles.profiles) {
      if (existing.contains(xmlProfile.extId))
        continue;
      UserProfile profile = new UserProfile(xmlProfile.extId, app);
      hiber().save(profile);
    }
    
    return Response.ok().build();
  }

  @GET
  @Path("questionary")
  @Transactional
  @SuppressWarnings("unchecked")
  public Response getQuestionary(@QueryParam("app") int appId) {

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

  @Transactional
  private Set<String> existingProfileExtIds(Set<String> extIds, App app) {
    if (extIds.isEmpty())
      return Collections.emptySet();

    List<UserProfile> profiles =  hiber()
            .createQuery("from UserProfile where extId in :extIds and app = :app")
            .setParameterList("extIds", extIds)
            .setParameter("app", app)
            .list();

    Set<String> existing = Sets.newHashSet();
    for (UserProfile profile : profiles)
      existing.add(profile.extId());
    return existing;
  }

  private static <T> T existing(T obj) throws WebApplicationException {
    if (obj == null)
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    return obj;
  }
}
