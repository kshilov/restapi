package com.heymoose.rest.resource;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.poll.BaseQuestion;
import com.heymoose.rest.domain.poll.Choice;
import com.heymoose.rest.domain.poll.Poll;
import com.heymoose.rest.domain.poll.Question;
import com.heymoose.rest.resource.xml.Mappers;
import com.heymoose.rest.resource.xml.XmlOrder;
import com.heymoose.rest.resource.xml.XmlQuestion;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

@Path("order")
@Singleton
public class OrderResource {

  private final static Logger log = LoggerFactory.getLogger(OrderResource.class);

  private final Provider<Session> sessionProvider;
  private final Properties settings;

  @Inject
  public OrderResource(Provider<Session> sessionProvider,
                       @Named("settings") Properties settings) {
    this.sessionProvider = sessionProvider;
    this.settings = settings;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @POST
  @Transactional
  public Response create(XmlOrder xmlOrder) {
    if (xmlOrder.questions == null || xmlOrder.questions.isEmpty())
      return Response.status(Response.Status.BAD_REQUEST).build();
    Targeting targeting = new Targeting(
            xmlOrder.targeting.age,
            xmlOrder.targeting.male,
            xmlOrder.targeting.city,
            xmlOrder.targeting.country
    );
    List<BaseQuestion> questions = questions(xmlOrder.questions);
    Order order = new Order(
            new BigDecimal(xmlOrder.balance),
            xmlOrder.name,
            targeting,
            new BigDecimal(settings.getProperty("question-cost")),
            questions,
            xmlOrder.questionary
    );
    hiber().save(order);
    return Response.ok(Integer.toString(order.id())).build();
  }

  private static List<BaseQuestion> questions(Iterable<XmlQuestion> from) {
    List<BaseQuestion> ret = Lists.newArrayList();
    for (XmlQuestion xmlQuestion : from) {
      if (xmlQuestion.poll) {
        List<Choice> choices = Lists.newArrayList();
        for (String choice : xmlQuestion.choices)
          choices.add(new Choice(choice));
        Poll poll = new Poll(xmlQuestion.text, choices);
        ret.add(poll);
      } else {
        Question question = new Question(xmlQuestion.text);
        ret.add(question);
      }
    }
    return ret;
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") int orderId) {
    Order order = (Order) hiber().get(Order.class, orderId);
    if (order == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    return Response.ok(Mappers.toXmlOrder(order)).build();
  }

  @POST
  @Path("{id}/balance")
  @Transactional
  public Response addToBalance(@PathParam("id") int orderId, @FormParam("amount") String amount) {
    Order order = (Order) hiber().get(Order.class, orderId);
    if (order == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    log.debug("Balance before: " + order.account().actual().balance().toString());
    try {
      order.account().addToBalance(new BigDecimal(amount), "replenishment");
    } catch (IllegalArgumentException e) {
      Response.status(Response.Status.BAD_REQUEST).build();
    }
    log.debug("Balance after: " + order.account().actual().balance().toString());
    return Response.ok().build();
  }
}
