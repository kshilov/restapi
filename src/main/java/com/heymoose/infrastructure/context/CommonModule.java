package com.heymoose.infrastructure.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.accounting.Withdrawal;
import com.heymoose.domain.accounting.WithdrawalPayment;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.errorinfo.ErrorInfo;
import com.heymoose.domain.errorinfo.ErrorInfoRepository;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.offer.CategoryGroup;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.offer.SubOfferRepository;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.settings.Setting;
import com.heymoose.domain.settings.Settings;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.AdminAccount;
import com.heymoose.domain.user.AdminAccountNotConfirmed;
import com.heymoose.domain.user.Site;
import com.heymoose.domain.user.User;
import com.heymoose.domain.user.UserRepository;
import com.heymoose.infrastructure.counter.BufferedClicks;
import com.heymoose.infrastructure.counter.BufferedShows;
import com.heymoose.infrastructure.persistence.ErrorInfoRepositoryHiber;
import com.heymoose.infrastructure.persistence.HibernateRepo;
import com.heymoose.infrastructure.persistence.IpSegment;
import com.heymoose.infrastructure.persistence.KeywordPattern;
import com.heymoose.infrastructure.persistence.KeywordPatternDao;
import com.heymoose.infrastructure.persistence.MlmExecution;
import com.heymoose.infrastructure.persistence.OfferGrantRepositoryHiber;
import com.heymoose.infrastructure.persistence.OfferRepositoryHiber;
import com.heymoose.infrastructure.persistence.SubOfferRepositoryHiber;
import com.heymoose.infrastructure.persistence.UserRepositoryHiber;
import com.heymoose.infrastructure.service.AccountingHiber;
import com.heymoose.infrastructure.service.Mlm;
import com.heymoose.infrastructure.service.OfferActionsStoredFunc;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.TrackingImpl;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.Properties;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());

    bind(Mlm.class);
    bind(Settings.class);
    bind(OfferLoader.class);
    bind(BufferedShows.class);
    bind(BufferedClicks.class);
    bind(KeywordPatternDao.class);
    bind(Accounting.class).to(AccountingHiber.class);
    bind(OfferActions.class).to(OfferActionsStoredFunc.class);

    bind(Tracking.class).to(TrackingImpl.class);
    bind(Repo.class).to(HibernateRepo.class);
    bind(UserRepository.class).to(UserRepositoryHiber.class);
    bind(OfferRepository.class).to(OfferRepositoryHiber.class);
    bind(SubOfferRepository.class).to(SubOfferRepositoryHiber.class);
    bind(OfferGrantRepository.class).to(OfferGrantRepositoryHiber.class);
    bind(ErrorInfoRepository.class).to(ErrorInfoRepositoryHiber.class);

    bindEntities(Offer.class, User.class, Banner.class,
        Setting.class, Site.class,
        BaseOffer.class, SubOffer.class, OfferGrant.class,
        Category.class, CategoryGroup.class,
        IpSegment.class, Account.class,
        AdminAccount.class, AccountingEntry.class, AccountingTransaction.class,
        OfferStat.class, OfferAction.class,
        AdminAccountNotConfirmed.class, Token.class, MlmExecution.class,
        KeywordPattern.class, ErrorInfo.class,
        Withdrawal.class, WithdrawalPayment.class,
        Product.class, ShopCategory.class, ProductAttribute.class, Tariff.class);
  }

  protected void bindEntities(Class... classes) {
    Multibinder<Class> multibinder = Multibinder.newSetBinder(binder(), Class.class, Names.named("entities"));
    for (Class klass : classes)
      multibinder.addBinding().toInstance(klass);
  }

  @Provides @Named("robokassaPass")  @Singleton
  protected String robokassaPass(@Named("settings") Properties settings) {
    return settings.getProperty("robokassaPass");
  }

  @Provides @Named("banners-dir")  @Singleton
  protected String bannersDir(@Named("settings") Properties settings) {
    String bannerDirStr = settings.getProperty("banners.dir");
    File bannerDir = new File(bannerDirStr);
    if (!bannerDir.exists())
      bannerDir.mkdirs();
    else if (!bannerDir.isDirectory())
      throw new IllegalArgumentException("Not a directory: "+ bannerDirStr);
    return bannerDirStr;
  }

  @Provides @Named("mlm-ratio") @Singleton
  protected double mlmRatio(@Named("settings") Properties settings) {
    return Double.parseDouble(settings.get("mlm-ratio").toString());
  }
}
