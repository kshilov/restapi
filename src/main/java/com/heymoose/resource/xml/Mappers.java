package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Accounts;
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
import com.heymoose.domain.Withdraw;
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
  
  public static XmlUsers toXmlUsers(Accounts accounts, Iterable<User> users) {
    return toXmlUsers(accounts, users, Details.WITH_RELATED_IDS);
  }
  
  public static XmlUsers toXmlUsers(Accounts accounts, Iterable<User> users, Details d) {
    XmlUsers xmlUsers = new XmlUsers();
    for (User user : users)
      xmlUsers.users.add(toXmlUser(accounts, user, d));
    return xmlUsers;
  }
  
  public static XmlUser toXmlUser(Accounts accounts, User user) {
    return toXmlUser(accounts, user, Details.WITH_RELATED_LISTS);
  }

  public static XmlUser toXmlUser(Accounts accounts, User user, Details d) {
    XmlUser xmlUser = new XmlUser();
    xmlUser.id = user.id();
    
    if (needFields(d)) {
      xmlUser.email = user.email();
      xmlUser.passwordHash = user.passwordHash();
      xmlUser.firstName = user.firstName();
      xmlUser.lastName = user.lastName();
      xmlUser.organization = user.organization();
      xmlUser.phone = user.phone();
      if (user.sourceUrl() != null)
        xmlUser.sourceUrl = user.sourceUrl().toString();
      if (user.messengerType() != null) {
        xmlUser.messengerType = user.messengerType().toString();
        xmlUser.messengerUid = user.messengerUid();
      }
      xmlUser.confirmed = user.confirmed();
      xmlUser.blocked = user.blocked();
      xmlUser.registerTime = user.registerTime().toString();
      
      if (user.customerAccount() != null)
        xmlUser.customerAccount = toXmlAccount(accounts, user.customerAccount());
      xmlUser.customerSecret = user.customerSecret();
      if (user.developerAccount() != null)
        xmlUser.developerAccount = toXmlAccount(accounts, user.developerAccount());
      xmlUser.roles = Sets.newHashSet();
      for (Role role : user.roles())
        xmlUser.roles.add(role.toString());
      
      if (needRelated(d)) {
        if (!user.apps().isEmpty()) {
          xmlUser.apps = Sets.newHashSet();
          for (App app : user.apps())
            xmlUser.apps.add(toXmlApp(accounts, app, relatedDetails(d)));
        }
        
        if (needRelatedLists(d)) {
          xmlUser.orders = Sets.newHashSet();
          for (Order order : user.orders())
            xmlUser.orders.add(toXmlOrder(accounts, order, relatedListDetails(d)));
        }
      }
    }
    return xmlUser;
  }
  
  public static XmlAccount toXmlAccount(Accounts accounts, Account account) {
    XmlAccount xmlAccount = new XmlAccount();
    xmlAccount.id = account.id();
    AccountTx lastTx = accounts.lastTxOf(account);
    if (lastTx != null)
      xmlAccount.balance = lastTx.balance().doubleValue();
    else
      xmlAccount.balance = 0.0;
    xmlAccount.allowNegativeBalance = account.allowNegativeBalance();
    return xmlAccount;
  }
  
  public static XmlOrders toXmlOrders(Accounts accounts, Iterable<Order> orders) {
    return toXmlOrders(accounts, orders, Details.WITH_RELATED_IDS);
  }

  public static XmlOrders toXmlOrders(Accounts accounts, Iterable<Order> orders, Details d) {
    XmlOrders xmlOrders = new XmlOrders();
    for (Order order : orders)
      xmlOrders.orders.add(toXmlOrder(accounts, order, d));
    return xmlOrders;
  }

  public static XmlOrder toXmlOrder(Accounts accounts, Order order) {
    return toXmlOrder(accounts, order, Details.WITH_RELATED_IDS);
  }

  public static XmlOrder toXmlOrder(Accounts accounts, Order order, Details d) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id();
    
    if (needFields(d)) {
      xmlOrder.disabled = order.disabled();
      xmlOrder.paused = order.paused();
      xmlOrder.cpa = order.cpa();
      xmlOrder.creationTime = order.creationTime().toString();
      xmlOrder.userId = order.customer().id();
      xmlOrder.account = toXmlAccount(accounts, order.account());
      
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
        xmlOrder.apps = toXmlApps(accounts, targeting.apps(), Details.ONLY_ENTITY);
      
      if (needRelated(d))
        xmlOrder.user = toXmlUser(accounts, order.customer(), relatedDetails(d));
    }
    return xmlOrder;
  }
  
  public static XmlApps toXmlApps(Accounts accounts, Iterable<App> apps) {
    return toXmlApps(accounts, apps, Details.WITH_RELATED_IDS);
  }
  
  public static XmlApps toXmlApps(Accounts accounts, Iterable<App> apps, Details d) {
    XmlApps xmlApps = new XmlApps();
    for (App app : apps)
      xmlApps.apps.add(toXmlApp(accounts, app, d));
    return xmlApps;
  }

  public static XmlApp toXmlApp(Accounts accounts, App app) {
    return toXmlApp(accounts, app, Details.WITH_RELATED_IDS);
  }

  public static XmlApp toXmlApp(Accounts accounts, App app, Details d) {
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
        xmlApp.user = toXmlUser(accounts, app.owner(), relatedDetails(d));
    }
    return xmlApp;
  }
  
  public static XmlActions toXmlActions(Accounts accounts, Iterable<Action> actions) {
    return toXmlActions(accounts, actions, Details.WITH_RELATED_IDS);
  }

  public static XmlActions toXmlActions(Accounts accounts, Iterable<Action> actions, Details d) {
    XmlActions xmlActions = new XmlActions();
    for (Action action : actions)
      xmlActions.actions.add(toXmlAction(accounts, action, d));
    return xmlActions;
  }
  
  public static XmlAction toXmlAction(Accounts accounts, Action action) {
    return toXmlAction(accounts, action, Details.WITH_RELATED_IDS);
  }

  public static XmlAction toXmlAction(Accounts accounts, Action action, Details d) {
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
        xmlAction.order = toXmlOrder(accounts, action.offer().order(), relatedDetails(d));
        xmlAction.app = toXmlApp(accounts, action.app(), relatedDetails(d));
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
    if (account.type() != null)
      xmlTransaction.type = account.type().toString();
    if (account.creationTime() != null)
      xmlTransaction.creationTime = account.creationTime().toString();
    if (account.endTime() != null)
      xmlTransaction.endTime = account.endTime().toString();
    return xmlTransaction;
  }
  
  public static XmlWithdraws toXmlWithdraws(long accountId, Iterable<Withdraw> withdraws) {
    XmlWithdraws xmlWithdraws = new XmlWithdraws();
    xmlWithdraws.accountId = accountId;
    for (Withdraw withdraw : withdraws)
      xmlWithdraws.withdraws.add(toXmlWithdraw(withdraw));
    return xmlWithdraws;
  }

  public static XmlWithdraw toXmlWithdraw(Withdraw withdraw) {
    XmlWithdraw xmlWithdraw = new XmlWithdraw();
    xmlWithdraw.id = withdraw.id();
    xmlWithdraw.amount = withdraw.amount().doubleValue();
    xmlWithdraw.done = withdraw.done();
    xmlWithdraw.timestamp = withdraw.timestamp().toString();
    return xmlWithdraw;
  }
}