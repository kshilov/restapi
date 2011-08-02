package com.heymoose.rest.resource;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.account.Accounts;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.question.Answer;
import com.heymoose.rest.domain.question.AnswerBase;
import com.heymoose.rest.domain.question.Answers;
import com.heymoose.rest.domain.question.QuestionBase;
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
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
  private final Provider<Integer> appProvider;
  private final Accounts accounts;

  private Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  private App app() {
    return (App) hiber().load(App.class, appProvider.get());
  }

  @Inject
  public ApiResource(Provider<Session> sessionProvider,
                     @Named("settings") Properties settings,
                     Questions questions,
                     Answers answers,
                     @Named("app") Provider<Integer> appProvider,
                     Accounts accounts) {
    this.sessionProvider = sessionProvider;
    this.questions = questions;
    this.answers = answers;
    this.appProvider = appProvider;
    this.accounts = accounts;
    this.maxShows = Integer.parseInt(settings.getProperty("max-shows"));
  }

  @PUT
  @Path("profiles")
  @Transactional
  public Response putUserProfiles(XmlProfiles profiles) {
    for (XmlProfile xmlProfile : profiles.profiles) {
      String extId = xmlProfile.profileId;
      // TODO: optimize
      UserProfile profile = profileBy(extId);
      if (profile == null) {
        profile = new UserProfile(extId, app());
        hiber().save(profile);
      }
    }
    return Response.ok().build();
  }

  @GET
  @Path("questions")
  @Transactional
  @SuppressWarnings("unchecked")
  public Response getQuestions(@QueryParam("count") int count, @QueryParam("extId") String extId) {
    UserProfile user = existing(profileBy(extId));

    String hql = "from Question q where " +
            "q.asked <= :maxShows " +
            "and q.form is null " +
            "and (select a from AnswerBase a inner join a.user u inner join a.question q1 where q1.id = q.id and u.extId = :extId) is null " +
            "and (q.id in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId and r.done = true) " +
            "or q.id not in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId))" +
            "order by rand()";
    
    // rand() is hack
    List<QuestionBase> questions = hiber()
                  .createQuery(hql)
                  .setParameter("maxShows", maxShows)
                  .setParameter("extId", extId)
                  .setMaxResults(count)
                  .setLockOptions(LockOptions.UPGRADE)
                  .list();

    try {
      for (QuestionBase question : questions)
        this.questions.reserve(question, user);
    } catch (IllegalStateException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    return Response.ok(Mappers.toXmlQuestions(questions)).build();
  }

  @GET
  @Path("polls")
  @Transactional
  @SuppressWarnings("unchecked")
  public Response getPolls(@QueryParam("count") int count, @QueryParam("extId") String extId) {
    UserProfile user = existing(profileBy(extId));
    String hql = "from Poll q where " +
                "q.asked <= :maxShows " +
                "and q.form is null " +
                "and (select a from AnswerBase a inner join a.user u inner join a.question q1 where q1.id = q.id and u.extId = :extId) is null " +
                "and (q.id in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId and r.done = true) " +
                "or q.id not in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId))" +
                "order by rand()";
    // rand() is hack
    List<QuestionBase> questions = hiber()
                  .createQuery(hql)
                  .setParameter("maxShows", maxShows)
                  .setParameter("extId", extId)
                  .setMaxResults(count)
                  .setLockOptions(LockOptions.UPGRADE)
                  .list();
    try {
      for (QuestionBase question : questions)
        this.questions.reserve(question, user);
    } catch (IllegalStateException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    return Response.ok(Mappers.toXmlQuestions(questions)).build();
  }

  @POST
  @Path("profiles")
  @Transactional
  public Response sendProfiles(XmlProfiles xmlProfiles) {
    Set<String> extIds = Sets.newHashSet();
    for (XmlProfile xmlProfile : xmlProfiles.profiles)
      extIds.add(xmlProfile.profileId);

    Set<String> existing = existingProfileExtIds(extIds, app());

    for (XmlProfile xmlProfile : xmlProfiles.profiles) {
      if (existing.contains(xmlProfile.profileId))
        continue;
      UserProfile profile = new UserProfile(xmlProfile.profileId, app());
      hiber().save(profile);
    }
    
    return Response.ok().build();
  }

  @GET
  @Path("form")
  @Transactional
  public Response getForm(@QueryParam("extId") String extId) {
    UserProfile user = existing(profileBy(extId));
    String hql = "from Form q where " +
                "q.asked <= :maxShows " +
                "and q.form is null " +
                "and (select a from AnswerBase a inner join a.user u inner join a.question q1 where q1.id in q.questions and u.extId = :extId) is null " +
                "and (q.id in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId and r.done = true) " +
                "or q.id not in (select t.id from Reservation r inner join r.user u inner join r.target t where u.extId = :extId))" +
                "order by rand()";
    Form form = (Form) hiber().createQuery("from Form f where f.asked <= :maxShows and f.id not in (select r.target.id from Reservation r where r.user.extId = :extId) order by rand()")
      .setParameter("maxShows", maxShows)
      .setParameter("extId", extId)
      .setMaxResults(1)
      .setLockOptions(LockOptions.UPGRADE)
      .uniqueResult();

    if (form == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    
    this.questions.reserve(form, user);
    return Response.ok(Mappers.toXmlQuestions(form.questions())).build();
  }

  @POST
  @Path("answers")
  @Transactional
  public Response sendAnswers(XmlAnswers xmlAnswers) {
    for (XmlAnswer xmlAnswer : xmlAnswers.answers) {
      AnswerBase answer = answer(xmlAnswer);
      hiber().saveOrUpdate(answer);
      answers.acceptAnswer(answer);
    }
    return Response.ok().build();
  }

  @DELETE
  @Path("question/{id}")
  @Transactional
  public Response returnQuestion(@PathParam("id") int questionId, @QueryParam("extId") String extId) {
    UserProfile user = profileBy(extId);
    Reservation reservation = (Reservation) hiber()
            .createQuery("from Reservation where user = :user target.id = :id")
            .setParameter("user", user)
            .setParameter("id", questionId)
            .uniqueResult();
    if (reservation != null) {
      Account reservationAccount = reservation.account();
      accounts.transfer(reservationAccount, reservation.target().order().account(), reservationAccount.actual().balance());
      reservation.cancel();
    }
    return Response.ok().build();
  }

  public AnswerBase answer(XmlAnswer xmlAnswer) {
    // TODO: optimize via batch
    AnswerBase existing = (AnswerBase) hiber()
            .createQuery("from AnswerBase where user.extId = :userId")
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
  private UserProfile profileBy(String extId) {
    return profileBy(app(), extId);
  }

  @Transactional
  private UserProfile profileBy(App app, String extId) {
    return (UserProfile) hiber()
              .createQuery("from UserProfile where app = :app and extId = :extId")
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
