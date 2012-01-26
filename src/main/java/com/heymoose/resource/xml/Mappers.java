package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Action;
import com.heymoose.domain.App;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.City;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferShow;
import com.heymoose.domain.Order;
import com.heymoose.domain.Performer;
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.Role;
import com.heymoose.domain.Targeting;
import com.heymoose.domain.User;
import com.heymoose.domain.VideoOffer;
import com.heymoose.util.HibernateUtil;

public class Mappers {
  
  private Mappers() {}
  
  public enum Details {
    ONLY_ID,
    ONLY_ENTITY,
    WITH_RELATED_IDS,
    WITH_RELATED_ENTITIES,
    WITH_RELATED_LISTS
  }
  
  private static boolean needFields(Details d) {
    return (d == Details.ONLY_ENTITY
         || d == Details.WITH_RELATED_IDS
         || d == Details.WITH_RELATED_ENTITIES
         || d == Details.WITH_RELATED_LISTS);
  }
  
  private static boolean needRelated(Details d) {
    return (d == Details.WITH_RELATED_IDS
         || d == Details.WITH_RELATED_ENTITIES
         || d == Details.WITH_RELATED_LISTS);
  }
  
  private static boolean needRelatedLists(Details d) {
    return d == Details.WITH_RELATED_LISTS;
  }
  
  private static Details relatedDetails(Details d) {
    if (d == Details.WITH_RELATED_ENTITIES ||
        d == Details.WITH_RELATED_LISTS)
      return Details.ONLY_ENTITY;
    return Details.ONLY_ID;
  }

  private static Details relatedListDetails(Details d) {
    return Details.ONLY_ENTITY;
  }
  
  public static XmlUsers toXmlUsers(Iterable<User> users) {
    return toXmlUsers(users, Details.WITH_RELATED_IDS);
  }
  
  public static XmlUsers toXmlUsers(Iterable<User> users, Details d) {
    XmlUsers xmlUsers = new XmlUsers();
    for (User user : users)
      xmlUsers.users.add(toXmlUser(user, d));
    return xmlUsers;
  }
  
  public static XmlUser toXmlUser(User user) {
    return toXmlUser(user, Details.WITH_RELATED_LISTS);
  }

  public static XmlUser toXmlUser(User user, Details d) {
    XmlUser xmlUser = new XmlUser();
    xmlUser.id = user.id();
    
    if (needFields(d)) {
      xmlUser.email = user.email();
      xmlUser.nickname = user.nickname();
      xmlUser.passwordHash = user.passwordHash();
      
      if (user.customerAccount() != null)
        xmlUser.customerAccount = toXmlAccount(user.customerAccount());
      xmlUser.customerSecret = user.customerSecret();
      if (user.developerAccount() != null)
        xmlUser.developerAccount = toXmlAccount(user.developerAccount());
      xmlUser.roles = Sets.newHashSet();
      for (Role role : user.roles())
        xmlUser.roles.add(role.toString());
      
      if (needRelated(d)) {
        if (!user.apps().isEmpty()) {
          xmlUser.apps = Sets.newHashSet();
          for (App app : user.apps())
            xmlUser.apps.add(toXmlApp(app, relatedDetails(d)));
        }
        
        if (needRelatedLists(d)) {
          xmlUser.orders = Sets.newHashSet();
          for (Order order : user.orders())
            xmlUser.orders.add(toXmlOrder(order, relatedListDetails(d)));
        }
      }
    }
    return xmlUser;
  }
  
  public static XmlAccount toXmlAccount(Account account) {
    XmlAccount xmlAccount = new XmlAccount();
    xmlAccount.id = account.id();
    AccountTx currentState = account.currentState();
    if (currentState != null)
      xmlAccount.balance = currentState.balance().doubleValue();
    else
      xmlAccount.balance = 0.0;
    return xmlAccount;
  }
  
  public static XmlOrders toXmlOrders(Iterable<Order> orders) {
    return toXmlOrders(orders, Details.WITH_RELATED_IDS);
  }

  public static XmlOrders toXmlOrders(Iterable<Order> orders, Details d) {
    XmlOrders xmlOrders = new XmlOrders();
    for (Order order : orders)
      xmlOrders.orders.add(toXmlOrder(order, d));
    return xmlOrders;
  }

  public static XmlOrder toXmlOrder(Order order) {
    return toXmlOrder(order, Details.WITH_RELATED_IDS);
  }

  public static XmlOrder toXmlOrder(Order order, Details d) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id();
    
    if (needFields(d)) {
      xmlOrder.disabled = order.disabled();
      xmlOrder.paused = order.paused();
      xmlOrder.cpa = order.cpa();
      xmlOrder.creationTime = order.creationTime().toString();
      xmlOrder.userId = order.customer().id();
      
      // Account fields
      xmlOrder.balance = order.account().currentState().balance().toString();
      xmlOrder.allowNegativeBalance = order.account().allowNegativeBalance();
      
      // Common offer fields
      Offer offer = order.offer();
      xmlOrder.offerId = offer.id();
      xmlOrder.title = offer.title();
      xmlOrder.url = offer.url();
      xmlOrder.autoApprove = offer.autoApprove();
      xmlOrder.reentrant = offer.reentrant();
      xmlOrder.type = offer.type().toString();

      offer = HibernateUtil.unproxy(offer);

      // Regular offer fields
      if (offer instanceof RegularOffer) {
        RegularOffer regularOffer = (RegularOffer) offer;
        xmlOrder.imageBase64 = regularOffer.imageBase64();
        xmlOrder.description = regularOffer.description();
      }
      
      // Video offer fields
      if (offer instanceof VideoOffer) {
        VideoOffer videoOffer = (VideoOffer) offer;
        xmlOrder.videoUrl = videoOffer.videoUrl();
      }

      // Banner offer fields
      if (offer instanceof BannerOffer) {
        BannerOffer bannerOffer = (BannerOffer) offer;
        xmlOrder.banners = newArrayList();
        for (Banner banner : bannerOffer.banners()) {
          XmlBanner xmlBanner = new XmlBanner();
          xmlBanner.id = banner.id();
          if (d == Details.WITH_RELATED_ENTITIES)
            xmlBanner.imageBase64 = banner.imageBase64();
          xmlBanner.mimeType = banner.mimeType();
          xmlBanner.bannerSize = toXmlBannerSize(banner.size(), d);
          xmlOrder.banners.add(xmlBanner);
        }
      }
      
      // Targeting fields
      Targeting targeting = order.targeting();
      xmlOrder.male = targeting.male();
      xmlOrder.minAge = targeting.minAge();
      xmlOrder.maxAge = targeting.maxAge();
      xmlOrder.minHour = targeting.minHour();
      xmlOrder.maxHour = targeting.maxHour();
      if (targeting.cityFilterType() != null)
        xmlOrder.cityFilterType = targeting.cityFilterType().toString();
      if (targeting.cities() != null)
        xmlOrder.cities = toXmlCities(targeting.cities());
      if (targeting.appFilterType() != null)
        xmlOrder.appFilterType = targeting.appFilterType().toString();
      if (targeting.apps() != null)
        xmlOrder.apps = toXmlApps(targeting.apps(), Details.ONLY_ENTITY);
      
      if (needRelated(d))
        xmlOrder.user = toXmlUser(order.customer(), relatedDetails(d));
    }
    return xmlOrder;
  }
  
  public static XmlApps toXmlApps(Iterable<App> apps) {
    return toXmlApps(apps, Details.WITH_RELATED_IDS);
  }
  
  public static XmlApps toXmlApps(Iterable<App> apps, Details d) {
    XmlApps xmlApps = new XmlApps();
    for (App app : apps)
      xmlApps.apps.add(toXmlApp(app, d));
    return xmlApps;
  }

  public static XmlApp toXmlApp(App app) {
    return toXmlApp(app, Details.WITH_RELATED_IDS);
  }

  public static XmlApp toXmlApp(App app, Details d) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.id = app.id();
    
    if (needFields(d)) {
      xmlApp.title = app.title();
      xmlApp.secret = app.secret();
      xmlApp.deleted = app.deleted();
      xmlApp.url = app.url().toString();
      xmlApp.callback = app.callback().toString();
      xmlApp.creationTime = app.creationTime().toString();
      xmlApp.userId = app.owner().id();
      
      if (app.platform() != null)
        xmlApp.platform = app.platform().toString();
      
      if (needRelated(d))
        xmlApp.user = toXmlUser(app.owner(), relatedDetails(d));
    }
    return xmlApp;
  }
  
  public static XmlActions toXmlActions(Iterable<Action> actions) {
    return toXmlActions(actions, Details.WITH_RELATED_IDS);
  }

  public static XmlActions toXmlActions(Iterable<Action> actions, Details d) {
    XmlActions xmlActions = new XmlActions();
    for (Action action : actions)
      xmlActions.actions.add(toXmlAction(action, d));
    return xmlActions;
  }
  
  public static XmlAction toXmlAction(Action action) {
    return toXmlAction(action, Details.WITH_RELATED_IDS);
  }

  public static XmlAction toXmlAction(Action action, Details d) {
    XmlAction xmlAction = new XmlAction();
    xmlAction.id = action.id();
    
    if (needFields(d)) {
      xmlAction.offerId = action.offer().id();
      xmlAction.performerId = action.performer().id();
      xmlAction.done = action.done();
      xmlAction.deleted = action.deleted();
      xmlAction.creationTime = action.creationTime().toString();
      if (action.approveTime() != null)
        xmlAction.approveTime = action.approveTime().toString();
      xmlAction.attempts = action.attempts();
      
      if (needRelated(d)) {
        xmlAction.performer = toXmlPerformer(action.performer(), relatedDetails(d));
        xmlAction.order = toXmlOrder(action.offer().order(), relatedDetails(d));
        xmlAction.app = toXmlApp(action.app(), relatedDetails(d));
      }
    }
    return xmlAction;
  }

  public static XmlOffers toXmlOffers(Iterable<Offer> offers) {
    XmlOffers xmlOffers = new XmlOffers();
    for (Offer offer : offers)
      xmlOffers.offers.add(toXmlOffer(offer));
    return xmlOffers;
  }

  public static XmlOffer toXmlOffer(Offer offer) {
    XmlOffer xmlOffer = new XmlOffer();
    xmlOffer.id = offer.id();
    xmlOffer.title = offer.title();
    xmlOffer.body = offer.url();
    return xmlOffer;
  }
  
  public static XmlPerformers toXmlPerformers(Iterable<Performer> performers) {
    return toXmlPerformers(performers, Details.WITH_RELATED_IDS);
  }
  
  public static XmlPerformers toXmlPerformers(Iterable<Performer> performers, Details d) {
    XmlPerformers xmlPerformers = new XmlPerformers();
    for (Performer performer : performers)
      xmlPerformers.performers.add(toXmlPerformer(performer, d));
    return xmlPerformers;
  }
  
  public static XmlPerformer toXmlPerformer(Performer performer) {
    return toXmlPerformer(performer, Details.WITH_RELATED_IDS);
  }
  
  public static XmlPerformer toXmlPerformer(Performer performer, Details d) {
    XmlPerformer xmlPerformer = new XmlPerformer();
    xmlPerformer.id = performer.id();
    
    if (needFields(d)) {
      xmlPerformer.extId = performer.extId();
      xmlPerformer.creationTime = performer.creationTime().toString();
      
      if (performer.platform() != null)
        xmlPerformer.platform = performer.platform().toString();
      xmlPerformer.male = performer.male();
      xmlPerformer.year = performer.year();
      xmlPerformer.city = performer.city();
      
      if (needRelated(d)) {
        if (performer.inviter() != null)
          xmlPerformer.inviter = toXmlPerformer(performer.inviter(), relatedDetails(d));
      }
    }
    return xmlPerformer;
  }
  
  public static XmlOfferShows toXmlOfferShows(Iterable<OfferShow> shows) {
    XmlOfferShows xmlOfferShows = new XmlOfferShows();
    for (OfferShow show : shows)
      xmlOfferShows.shows.add(toXmlOfferShow(show));
    return xmlOfferShows;
  }
  
  public static XmlOfferShow toXmlOfferShow(OfferShow show) {
    XmlOfferShow xmlOfferShow = new XmlOfferShow();
    xmlOfferShow.id = show.id();
    xmlOfferShow.showTime = show.showTime().toString();
    return xmlOfferShow;
  }
  
  public static XmlCount toXmlCount(Long count) {
    XmlCount xmlCount = new XmlCount();
    xmlCount.count = count;
    return xmlCount;
  }
  
  public static XmlBannerSizes toXmlBannerSizes(Iterable<BannerSize> bannerSizes) {
    return toXmlBannerSizes(bannerSizes, Details.WITH_RELATED_IDS);
  }

  public static XmlBannerSizes toXmlBannerSizes(Iterable<BannerSize> bannerSizes, Details d) {
    XmlBannerSizes xmlBannerSizes = new XmlBannerSizes();
    for (BannerSize bannerSize : bannerSizes)
      xmlBannerSizes.bannerSizes.add(toXmlBannerSize(bannerSize, d));
    return xmlBannerSizes;
  }
  
  public static XmlBannerSize toXmlBannerSize(BannerSize bannerSize) {
    return toXmlBannerSize(bannerSize, Details.WITH_RELATED_IDS);
  }

  public static XmlBannerSize toXmlBannerSize(BannerSize bannerSize, Details d) {
    XmlBannerSize xmlBannerSize = new XmlBannerSize();
    xmlBannerSize.id = bannerSize.id();
    
    if (needFields(d)) {
      xmlBannerSize.width = bannerSize.width();
      xmlBannerSize.height = bannerSize.height();
      xmlBannerSize.disabled = bannerSize.disabled();
    }
    return xmlBannerSize;
  }

  public static XmlCities toXmlCities(Iterable<City> cities) {
    XmlCities xmlCities = new XmlCities();
    for (City city : cities)
      xmlCities.cities.add(toXmlCity(city));
    return xmlCities;
  }

  public static XmlCity toXmlCity(City city) {
    XmlCity xmlCity = new XmlCity();
    xmlCity.id = city.id();
    xmlCity.name = city.name();
    xmlCity.disabled = city.disabled();
    return xmlCity;
  }
  
  public static XmlTransactions toXmlTransactions(Iterable<AccountTx> transactions, int count) {
    XmlTransactions xmlTransactions = new XmlTransactions();
    for (AccountTx tx : transactions)
      xmlTransactions.transactions.add(toXmlTransaction(tx));
    xmlTransactions.count = count;
    return xmlTransactions;
  }
  
  public static XmlTransaction toXmlTransaction(AccountTx account) {
    XmlTransaction xmlTransaction = new XmlTransaction();
    xmlTransaction.id = account.id();
    xmlTransaction.balance = account.balance().doubleValue();
    xmlTransaction.diff = account.diff().doubleValue();
    xmlTransaction.description = account.description();
    return xmlTransaction;
  }
}
