package com.heymoose.resource.xml;

import com.google.common.collect.Sets;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.SubOffer;
import java.util.Map;

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
        xmlUser.customerAccount = toXmlAccount(user.customerAccount());
      xmlUser.customerSecret = user.customerSecret();
      if (user.developerAccount() != null)
        xmlUser.developerAccount = toXmlAccount(user.developerAccount());
      xmlUser.roles = Sets.newHashSet();
      for (Role role : user.roles())
        xmlUser.roles.add(role.toString());
    }
    return xmlUser;
  }

  public static XmlAccount toXmlAccount(Account account) {
    XmlAccount xmlAccount = new XmlAccount();
    xmlAccount.id = account.id();
    xmlAccount.balance = account.balance().doubleValue();
    xmlAccount.allowNegativeBalance = account.allowNegativeBalance();
    return xmlAccount;
  }

  public static XmlCount toXmlCount(Long count) {
    XmlCount xmlCount = new XmlCount();
    xmlCount.count = count;
    return xmlCount;
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
  
  
  public static XmlNewOffer toXmlNewOffer(Offer offer) {
    XmlNewOffer xmlNewOffer = new XmlNewOffer();
    xmlNewOffer.id = offer.id();
    xmlNewOffer.advertiser = toXmlUser(offer.advertiser());
    xmlNewOffer.account = toXmlAccount(offer.account());
    xmlNewOffer.payMethod = offer.payMethod().toString();
    if (offer.cpaPolicy() != null)
      xmlNewOffer.cpaPolicy = offer.cpaPolicy().toString();
    xmlNewOffer.name = offer.name();
    xmlNewOffer.description = offer.description();
    xmlNewOffer.logoFileName = offer.logoFileName();
    xmlNewOffer.cost = offer.cost();
    xmlNewOffer.percent = offer.percent();
    xmlNewOffer.approved = offer.approved();
    xmlNewOffer.active = offer.active();
    xmlNewOffer.creationTime = offer.creationTime().toString();
    xmlNewOffer.title = offer.title();
    xmlNewOffer.url = offer.url();
    xmlNewOffer.autoApprove = offer.autoApprove();
    xmlNewOffer.reentrant = offer.reentrant();
    
    for (SubOffer suboffer : offer.suboffers())
      xmlNewOffer.suboffers.add(toXmlSubOffer(suboffer));
    
    for (Region region : offer.regions())
      xmlNewOffer.regions.add(region.toString());
    return xmlNewOffer;
  }
  
  public static XmlNewOffer toXmlGrantedNewOffer(OfferGrant grant) {
    XmlNewOffer xmlNewOffer = toXmlNewOffer(grant.offer());
    xmlNewOffer.grant = toXmlOfferGrant(grant, false);
    return xmlNewOffer;
  }
  
  public static XmlNewOffers toXmlNewOffers(Iterable<Offer> offers, Long count) {
    XmlNewOffers xmlNewOffers = new XmlNewOffers();
    xmlNewOffers.count = count;
    for (Offer offer : offers)
      xmlNewOffers.offers.add(toXmlNewOffer(offer));
    return xmlNewOffers;
  }
  
  public static XmlNewOffers toXmlNewOffers(Iterable<Offer> offers,
                                            Map<Long, OfferGrant> grants, Long count) {
    XmlNewOffers xmlNewOffers = toXmlNewOffers(offers, count);
    for (XmlNewOffer xmlNewOffer : xmlNewOffers.offers)
      if (grants.containsKey(xmlNewOffer.id))
        xmlNewOffer.grant = toXmlOfferGrant(grants.get(xmlNewOffer.id), false);
    return xmlNewOffers;
  }
  
  public static XmlNewOffers toXmlGrantedNewOffers(Iterable<OfferGrant> grants, Long count) {
    XmlNewOffers xmlNewOffers = new XmlNewOffers();
    xmlNewOffers.count = count;
    for (OfferGrant grant : grants)
      xmlNewOffers.offers.add(toXmlGrantedNewOffer(grant));
    return xmlNewOffers;
  }
  
  public static XmlSubOffer toXmlSubOffer(SubOffer offer) {
    XmlSubOffer xmlSubOffer = new XmlSubOffer();
    xmlSubOffer.id = offer.id();
    xmlSubOffer.cpaPolicy = offer.cpaPolicy().toString();
    xmlSubOffer.cost = offer.cost();
    xmlSubOffer.percent = offer.percent();
    xmlSubOffer.active = offer.active();
    xmlSubOffer.creationTime = offer.creationTime().toString();
    xmlSubOffer.title = offer.title();
    xmlSubOffer.autoApprove = offer.autoApprove();
    xmlSubOffer.reentrant = offer.reentrant();
    return xmlSubOffer;
  }
  
  public static XmlSubOffers toXmlSubOffers(Iterable<SubOffer> suboffers, Long count) {
    XmlSubOffers xmlSubOffers = new XmlSubOffers();
    xmlSubOffers.count = count;
    for (SubOffer suboffer : suboffers)
      xmlSubOffers.suboffers.add(toXmlSubOffer(suboffer));
    return xmlSubOffers;
  }
  
  public static XmlOfferGrant toXmlOfferGrant(OfferGrant grant, boolean full) {
    XmlOfferGrant xmlOfferGrant = new XmlOfferGrant();
    xmlOfferGrant.id = grant.id();
    xmlOfferGrant.message = grant.message();
    xmlOfferGrant.backUrl = grant.backUrl();
    xmlOfferGrant.postbackUrl = grant.postBackUrl();
    xmlOfferGrant.state = grant.state().name();
    xmlOfferGrant.blocked = grant.blocked();
    xmlOfferGrant.rejectReason = grant.rejectReason();
    xmlOfferGrant.blockReason = grant.blockReason();

    if (full) {
      xmlOfferGrant.offer = toXmlNewOffer(grant.offer());
      xmlOfferGrant.affiliate = toXmlUser(grant.affiliate());
    } else {
      xmlOfferGrant.offer = new XmlNewOffer();
      xmlOfferGrant.offer.id = grant.offerId();
      xmlOfferGrant.affiliate = new XmlUser();
      xmlOfferGrant.affiliate.id = grant.affiliateId();
    }
    return xmlOfferGrant;
  }
  
  public static XmlOfferGrants toXmlOfferGrants(Iterable<OfferGrant> grants, Long count, boolean full) {
    XmlOfferGrants xmlOfferGrants = new XmlOfferGrants();
    xmlOfferGrants.count = count;
    for (OfferGrant grant : grants)
      xmlOfferGrants.grants.add(toXmlOfferGrant(grant, full));
    return xmlOfferGrants;
  }

  public static XmlCategories toXmlCategories(Iterable<Category> categories) {
    XmlCategories xmlCategories = new XmlCategories();
    for (Category category : categories)
      xmlCategories.categories.add(toXmlCategory(category));
    return xmlCategories;
  }

  public static XmlCategory toXmlCategory(Category category) {
    XmlCategory xmlCategory = new XmlCategory();
    xmlCategory.id = category.id();
    xmlCategory.grouping = category.grouping();
    xmlCategory.name = category.name();
    return xmlCategory;
  }
}