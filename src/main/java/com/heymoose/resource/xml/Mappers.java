package com.heymoose.resource.xml;

import com.heymoose.domain.Action;
import com.heymoose.domain.App;
import com.heymoose.domain.Order;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;

public class Mappers {
  
  private Mappers() {}

  public static XmlUser toXmlUser(User user, boolean full) {
    XmlUser xmlUser = new XmlUser();
    xmlUser.id = user.id();
    xmlUser.email = user.email();
    xmlUser.nickname = user.nickname();
    xmlUser.passwordHash = user.passwordHash();
    if (user.customerAccount() != null) {
      if (user.customerAccount().currentState() == null)
        xmlUser.customerAccount = "0.0";
      else
        xmlUser.customerAccount = user.customerAccount().currentState().balance().toString();
    }
    if (user.developerAccount() != null) {
      if (user.developerAccount().currentState() == null)
        xmlUser.developerAccount = "0.0";
      else
        xmlUser.developerAccount = user.developerAccount().currentState().balance().toString();
    }
    for (Role role : user.roles())
      xmlUser.roles.add(role.toString());
    if (!full)
      return xmlUser;
    for (Order order : user.orders())
      xmlUser.orders.add(toXmlOrder(order));
    if (!user.apps().isEmpty())
      xmlUser.app = toXmlApp(user.apps().iterator().next());
    return xmlUser;
  }

  public static XmlOrders toXmlOrders(Iterable<Order> orders) {
    XmlOrders xmlOrders = new XmlOrders();
    for (Order order : orders)
      xmlOrders.orders.add(toXmlOrder(order));
    return xmlOrders;
  }

  public static XmlOrder toXmlOrder(Order order) {
    return toXmlOrder(order, false);
  }

  public static XmlOrder toXmlOrder(Order order, boolean full) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id();
    xmlOrder.balance = order.account().currentState().balance().toString();
    xmlOrder.title = order.offer().title();
    xmlOrder.approved = order.approved();
    xmlOrder.deleted = order.deleted();
    if (full)
      xmlOrder.userId = order.customer().id();
    return xmlOrder;
  }

  public static XmlApp toXmlApp(App app) {
    return toXmlApp(app, false);
  }

  public static XmlApp toXmlApp(App app, boolean full) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.id = app.id();
    xmlApp.secret = app.secret();
    xmlApp.deleted = app.deleted();
    xmlApp.callback = app.callback().toString();
    if (full)
      xmlApp.userId = app.owner().id();
    return xmlApp;
  }

  public static XmlActions toXmlActions(Iterable<Action> actions) {
    XmlActions xmlActions = new XmlActions();
    for (Action action : actions)
      xmlActions.actions.add(toXmlAction(action));
    return xmlActions;
  }

  private static XmlAction toXmlAction(Action action) {
    XmlAction xmlAction = new XmlAction();
    xmlAction.id = action.id();
    xmlAction.offerId = action.offer().id();
    xmlAction.performerId = action.performer().id();
    xmlAction.done = action.done();
    xmlAction.deleted = action.deleted();
    xmlAction.creationTime = action.creationTime().toString();
    return xmlAction;
  }
}
