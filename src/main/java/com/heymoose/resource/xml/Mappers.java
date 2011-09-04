package com.heymoose.resource.xml;

import com.heymoose.domain.App;
import com.heymoose.domain.Order;
import com.heymoose.domain.User;

public class Mappers {
  
  private Mappers() {}

  public static XmlUser toXmlUser(User user) {
    XmlUser xmlUser = new XmlUser();
    xmlUser.id = user.id();
    xmlUser.email = user.email;
    xmlUser.nickname = user.nickname;
    if (user.orders != null)
      for (Order order : user.orders)
        xmlUser.orders.add(toXmlOrder(order));
    if (user.apps != null && !user.apps.isEmpty())
      xmlUser.app = toXmlApp(user.apps.iterator().next());
    return xmlUser;
  }

  public static XmlOrder toXmlOrder(Order order) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id();
    xmlOrder.balance = order.account.actual().balance().toString();
    xmlOrder.title = order.offer.action.title;
    return xmlOrder;
  }

  public static XmlApp toXmlApp(App app) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.id = app.id();
    xmlApp.secret = app.secret;
    return xmlApp;
  }
}
