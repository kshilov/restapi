package com.heymoose.resource.xml;

import com.google.common.collect.Sets;
import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.user.Role;
import com.heymoose.domain.user.User;
import com.heymoose.domain.accounting.Withdraw;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.errorinfo.ErrorInfo;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.util.SqlLoader;
import java.util.List;
import java.util.Map;

public class Mappers {

  private Mappers() {
  }

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
      xmlUser.wmr = user.wmr();
      xmlUser.confirmed = user.confirmed();
      xmlUser.blocked = user.blocked();
      xmlUser.blockReason = user.blockReason();
      xmlUser.registerTime = user.registerTime().toString();

      if (user.advertiserAccount() != null) {
        xmlUser.customerAccount = toXmlAccount(user.advertiserAccount());
        xmlUser.advertiserAccount = toXmlAccount(user.advertiserAccount());
      }
      if (user.affiliateAccount() != null) {
        xmlUser.developerAccount = toXmlAccount(user.affiliateAccount());
        xmlUser.affiliateAccount = toXmlAccount(user.affiliateAccount());
      }
      if (user.affiliateAccountNotConfirmed() != null) {
        xmlUser.developerAccountNotConfirmed = toXmlAccount(user.affiliateAccountNotConfirmed());
        xmlUser.affiliateAccountNotConfirmed = toXmlAccount(user.affiliateAccountNotConfirmed());
      }
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

  public static XmlAccountingEntry toXmlAccountingEntry(AccountingEntry entry) {
    XmlAccountingEntry xmlEntry = new XmlAccountingEntry();
    xmlEntry.id = entry.id();
    xmlEntry.amount = entry.amount();
    xmlEntry.descr = entry.descr();
    if (entry.event() != null)
      xmlEntry.event = entry.event().toString();
    xmlEntry.creationTime = entry.creationTime().toString();
    return xmlEntry;
  }

  public static XmlAccountingEntries toXmlAccountingEntries(Iterable<AccountingEntry> entries, Long count) {
    XmlAccountingEntries xmlEntries = new XmlAccountingEntries();
    xmlEntries.count = count;
    for (AccountingEntry entry : entries)
      xmlEntries.entries.add(toXmlAccountingEntry(entry));
    return xmlEntries;
  }

  public static XmlCount toXmlCount(Long count) {
    XmlCount xmlCount = new XmlCount();
    xmlCount.count = count;
    return xmlCount;
  }

  public static XmlWithdraws toXmlWithdraws(List<Object[]> withdraws, Long count, Object nonApprovedCount, Object nonApprovedTotal) {
    XmlWithdraws xmlWithdraws = new XmlWithdraws();
    xmlWithdraws.count = count;
    xmlWithdraws.nonApprovedCount = SqlLoader.extractLong(nonApprovedCount);
    xmlWithdraws.nonApprovedTotal = SqlLoader.extractDouble(nonApprovedTotal);
    for (Object[] withdraw : withdraws) {
      XmlWithdraw xmlWithdraw = new XmlWithdraw();
      xmlWithdraw.id = SqlLoader.extractLong(withdraw[0]);
      xmlWithdraw.amount = SqlLoader.extractDouble(withdraw[1]);
      xmlWithdraw.timestamp = SqlLoader.extractDateTime(withdraw[2]).toString();
      xmlWithdraw.done = SqlLoader.extractBoolean(withdraw[3]);
      xmlWithdraw.affId = SqlLoader.extractLong(withdraw[4]);
      xmlWithdraw.affEmail = SqlLoader.extractString(withdraw[5]);
      xmlWithdraws.withdraws.add(xmlWithdraw);
    }
    return xmlWithdraws;
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


  public static XmlOffer toXmlOffer(Offer offer) {
    XmlOffer xmlOffer = new XmlOffer();
    xmlOffer.id = offer.id();
    xmlOffer.advertiser = toXmlUser(offer.advertiser());
    xmlOffer.account = toXmlAccount(offer.account());
    xmlOffer.payMethod = offer.payMethod().toString();
    if (offer.cpaPolicy() != null)
      xmlOffer.cpaPolicy = offer.cpaPolicy().toString();
    xmlOffer.name = offer.name();
    xmlOffer.description = offer.description();
    xmlOffer.shortDescription = offer.shortDescription();
    xmlOffer.cr = offer.cr();
    xmlOffer.showcase = offer.showcase();
    xmlOffer.logoFileName = offer.logoFileName();
    xmlOffer.cost = offer.cost();
    xmlOffer.cost2 = offer.cost2();
    xmlOffer.percent = offer.percent();
    xmlOffer.affiliateFee = offer.fee();
    xmlOffer.affiliateFeeType = offer.feeType().toString();
    xmlOffer.approved = offer.approved();
    xmlOffer.active = offer.active();
    xmlOffer.blockReason = offer.blockReason();
    xmlOffer.creationTime = offer.creationTime().toString();
    if (offer.launchTime() != null)
      xmlOffer.launchTime = offer.launchTime().toString();
    xmlOffer.allowDeeplink = offer.allowDeeplink();
    xmlOffer.title = offer.title();
    xmlOffer.url = offer.url();
    xmlOffer.siteUrl = offer.siteUrl();
    xmlOffer.autoApprove = offer.autoApprove();
    xmlOffer.reentrant = offer.reentrant();
    xmlOffer.code = offer.code();
    xmlOffer.holdDays = offer.holdDays();
    xmlOffer.cookieTtl = offer.cookieTtl();
    xmlOffer.tokenParamName = offer.tokenParamName();
    xmlOffer.exclusive = offer.exclusive();

    if (!offer.exclusive()) {
      for (SubOffer suboffer : offer.suboffers())
        xmlOffer.suboffers.add(toXmlSubOffer(suboffer));
    }

    for (Category category : offer.categories())
      xmlOffer.categories.add(toXmlCategory(category));

    for (Banner banner : offer.banners())
      xmlOffer.banners.add(toXmlBanner(banner));

    for (String region : offer.regions())
      xmlOffer.regions.add(region);

    return xmlOffer;
  }

  public static XmlOffer toXmlGrantedNewOffer(OfferGrant grant) {
    XmlOffer xmlOffer = toXmlOffer(grant.offer());
    xmlOffer.grant = toXmlOfferGrant(grant, false);
    return xmlOffer;
  }

  public static XmlOffers toXmlOffers(Iterable<Offer> offers, Long count) {
    XmlOffers xmlOffers = new XmlOffers();
    xmlOffers.count = count;
    for (Offer offer : offers)
      xmlOffers.offers.add(toXmlOffer(offer));
    return xmlOffers;
  }

  public static XmlOffers toXmlOffers(Iterable<Offer> offers,
                                      Map<Long, OfferGrant> grants, Long count) {
    XmlOffers xmlOffers = toXmlOffers(offers, count);
    for (XmlOffer xmlOffer : xmlOffers.offers)
      if (grants.containsKey(xmlOffer.id))
        xmlOffer.grant = toXmlOfferGrant(grants.get(xmlOffer.id), false);
    return xmlOffers;
  }

  public static XmlOffers toXmlGrantedOffers(Iterable<OfferGrant> grants, Long count) {
    XmlOffers xmlOffers = new XmlOffers();
    xmlOffers.count = count;
    for (OfferGrant grant : grants)
      xmlOffers.offers.add(toXmlGrantedNewOffer(grant));
    return xmlOffers;
  }

  public static XmlSubOffer toXmlSubOffer(SubOffer offer) {
    XmlSubOffer xmlSubOffer = new XmlSubOffer();
    xmlSubOffer.id = offer.id();
    xmlSubOffer.payMethod = offer.payMethod().toString();
    xmlSubOffer.cpaPolicy = offer.cpaPolicy().toString();
    xmlSubOffer.cost = offer.cost();
    xmlSubOffer.cost2 = offer.cost2();
    xmlSubOffer.percent = offer.percent();
    xmlSubOffer.active = offer.active();
    xmlSubOffer.creationTime = offer.creationTime().toString();
    xmlSubOffer.title = offer.title();
    xmlSubOffer.autoApprove = offer.autoApprove();
    xmlSubOffer.reentrant = offer.reentrant();
    xmlSubOffer.code = offer.code();
    xmlSubOffer.holdDays = offer.holdDays();
    xmlSubOffer.exclusive = offer.exclusive();
    xmlSubOffer.affiliateFee = offer.fee();
    xmlSubOffer.affiliateFeeType = offer.feeType().toString();
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
      xmlOfferGrant.offer = toXmlOffer(grant.offer());
      xmlOfferGrant.affiliate = toXmlUser(grant.affiliate());
    } else {
      xmlOfferGrant.offer = new XmlOffer();
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
    xmlCategory.grouping = category.group().name();
    xmlCategory.name = category.name();
    return xmlCategory;
  }

  public static XmlBanner toXmlBanner(Banner banner) {
    XmlBanner xmlBanner = new XmlBanner();
    xmlBanner.id = banner.id();
    xmlBanner.width = banner.width();
    xmlBanner.height = banner.height();
    xmlBanner.mimeType = banner.mimeType();
    xmlBanner.url = banner.url();
    return xmlBanner;
  }

  public static XmlErrorInfo toXmlErrorInfo(ErrorInfo error) {
    return new XmlErrorInfo(error);
  }

  public static XmlErrorsInfo toXmlErrorsInfo(Iterable<ErrorInfo> info,
                                              Long count) {
    XmlErrorsInfo xmlInfo = new XmlErrorsInfo();
    for (ErrorInfo error : info) {
      xmlInfo.list.add(toXmlErrorInfo(error));
    }
    xmlInfo.count = count;
    return xmlInfo;
  }

  public static XmlRegions toXmlRegions(Map<String, String> countriesByCode) {
    XmlRegions xmlRegions = new XmlRegions();
    for (Map.Entry<String, String> ent : countriesByCode.entrySet())
      xmlRegions.regions.add(toXmlRegion(ent.getKey(), ent.getValue()));
    return xmlRegions;
  }

  public static XmlRegion toXmlRegion(String countryCode, String countryName) {
    XmlRegion xmlRegion = new XmlRegion();
    xmlRegion.countryCode = countryCode;
    xmlRegion.countryName = countryName;
    return xmlRegion;
  }
}
