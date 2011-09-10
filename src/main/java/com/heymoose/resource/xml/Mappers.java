package com.heymoose.resource.xml;

import com.heymoose.domain.App;
import com.heymoose.domain.Order;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;

public class Mappers {
  
  private Mappers() {}

  public static XmlUser toXmlUser(User user, boolean full) {
    XmlUser xmlUser = new XmlUser();
    xmlUser.id = user.id;
    xmlUser.email = user.email;
    xmlUser.nickname = user.nickname;
    xmlUser.passwordHash = user.passwordHash;
    if (user.roles != null)
      for (Role role : user.roles)
        xmlUser.roles.add(role.toString());
    if (!full)
      return xmlUser;
    if (user.orders != null)
      for (Order order : user.orders)
        xmlUser.orders.add(toXmlOrder(order));
    if (user.apps != null && !user.apps.isEmpty())
      xmlUser.app = toXmlApp(user.apps.iterator().next());
    return xmlUser;
  }

  public static XmlOrder toXmlOrder(Order order) {
    return toXmlOrder(order, false);
  }

  public static XmlOrder toXmlOrder(Order order, boolean full) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id;
    xmlOrder.balance = order.account.actual().balance().toString();
    xmlOrder.title = order.offer.title;
    if (full)
      xmlOrder.userId = order.user.id;
    return xmlOrder;
  }

  public static XmlApp toXmlApp(App app) {
    return toXmlApp(app, false);
  }

  public static XmlApp toXmlApp(App app, boolean full) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.id = app.id;
    xmlApp.secret = app.secret;
    if (full)
      xmlApp.userId = app.user.id;
    return xmlApp;
  }
}
