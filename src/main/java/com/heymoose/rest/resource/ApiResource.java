package com.heymoose.rest.resource;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.question.Answer;
import com.heymoose.rest.domain.question.Answers;
import com.heymoose.rest.domain.question.BaseAnswer;
import com.heymoose.rest.domain.question.BaseQuestion;
import com.heymoose.rest.domain.question.Choice;
import com.heymoose.rest.domain.question.Form;
import com.heymoose.rest.domain.question.Poll;
import com.heymoose.rest.domain.question.Question;
import com.heymoose.rest.domain.question.Questions;
import com.heymoose.rest.domain.question.Vote;
import com.heymoose.rest.security.Secured;
import com.heymoose.rest.resource.xml.Mappers;
import com.heymoose.rest.resource.xml.XmlAnswer;
import com.heymoose.rest.resource.xml.XmlAnswers;
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
import java.io.Serializable;
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
  private final Answers answers;

  private Session hiber() {
    return sessionProvider.get();
  }

  @Inject
  public ApiResource(Provider<Session> sessionProvider, @Named("settings") Properties settings, Questions questions, Answers answers) {
    this.sessionProvider = sessionProvider;
    this.questions = questions;
    this.answers = answers;
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
      String extId = xmlProfile.profileId;
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
              .createQuery("from BaseQuestion where asked <= :maxShows and form is null")
              .setParameter("maxShows", maxShows)
              .setMaxResults(count)
              .list();

    try {
      // WARNING: may be races
      for (BaseQuestion question : questions)
        this.questions.reserve(question);
    } catch (IllegalStateException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }

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
      extIds.add(xmlProfile.profileId);

    Set<String> existing = existingProfileExtIds(extIds, app);

    for (XmlProfile xmlProfile : xmlProfiles.profiles) {
      if (existing.contains(xmlProfile.profileId))
        continue;
      UserProfile profile = new UserProfile(xmlProfile.profileId, app);
      hiber().save(profile);
    }
    
    return Response.ok().build();
  }

  @GET
  @Path("form")
  @Transactional
  public Response getForm(@QueryParam("app") int appId) {
    App app = existing((App) hiber().get(App.class, appId));

    Form form = (Form) hiber().createQuery("from Form where asked <= :maxShows")
      .setParameter("maxShows", maxShows)
      .setMaxResults(1)
      .uniqueResult();

    if (form == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    
    // WARNING: may be races
    this.questions.reserve(form);

    // TODO: check for user answers by extId
    return Response.ok(Mappers.toXmlQuestions(form.questions())).build();
  }

  @POST
  @Path("answers")
  @Transactional
  public Response sendAnswers(@QueryParam("app") int appId, XmlAnswers xmlAnswers) {
    for (XmlAnswer xmlAnswer : xmlAnswers.answers) {
      BaseAnswer answer = answer(xmlAnswer);
      hiber().saveOrUpdate(answer);
      answers.acceptAnswer(answer);
    }
    return Response.ok().build();
  }

  public BaseAnswer answer(XmlAnswer xmlAnswer) {
    // TODO: optimize via batch
    BaseAnswer existing = (BaseAnswer) hiber()
            .createQuery("from BaseAnswer where user.extId = :userId")
            .setParameter("userId", xmlAnswer.profileId)
            .uniqueResult();
    if (existing != null)
      return existing;

    UserProfile profile = (UserProfile) hiber()
            .createQuery("from UserProfile where extId = :extId")
            .setParameter("extId", xmlAnswer.profileId)
            .uniqueResult();

    if (xmlAnswer.vote)
      return new Vote(
              existing(Poll.class, xmlAnswer.questionId),
              existing(profile),
              existing(Choice.class, xmlAnswer.choice)
      );
    else
      return new Answer(
              existing(Question.class, xmlAnswer.questionId),
              existing(profile),
              xmlAnswer.text
      );
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

    @SuppressWarnings("unchecked")
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

  public <T> T existing(Class<T> klass, Serializable id) {
    return existing((T) hiber().get(klass, id));
  }
}
