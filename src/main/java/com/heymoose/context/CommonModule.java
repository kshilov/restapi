package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.heymoose.domain.model.AdminAccount;
import com.heymoose.domain.model.AdminAccountNotConfirmed;
import com.heymoose.domain.model.Banner;
import com.heymoose.domain.model.IpSegment;
import com.heymoose.domain.model.KeywordPattern;
import com.heymoose.domain.service.Accounting;
import com.heymoose.domain.service.OfferActions;
import com.heymoose.infrastructure.AccountingHiber;
import com.heymoose.infrastructure.offer.OfferLoader;
import com.heymoose.infrastructure.TrackingImpl;
import com.heymoose.infrastructure.actions.OfferActionsStoredFunc;
import com.heymoose.infrastructure.hibernate.KeywordPatternDao;
import com.heymoose.domain.model.Site;
import com.heymoose.domain.model.Token;
import com.heymoose.domain.model.User;
import com.heymoose.domain.model.UserRepository;
import com.heymoose.domain.model.Withdraw;
import com.heymoose.domain.model.accounting.Account;
import com.heymoose.domain.model.accounting.AccountingEntry;
import com.heymoose.domain.model.accounting.AccountingTransaction;
import com.heymoose.domain.model.action.OfferAction;
import com.heymoose.domain.model.errorinfo.ErrorInfo;
import com.heymoose.domain.model.errorinfo.ErrorInfoRepository;
import com.heymoose.infrastructure.hibernate.ErrorInfoRepositoryHiber;
import com.heymoose.domain.model.grant.OfferGrant;
import com.heymoose.domain.model.grant.OfferGrantRepository;
import com.heymoose.infrastructure.hibernate.OfferGrantRepositoryHiber;
import com.heymoose.infrastructure.hibernate.HibernateRepo;
import com.heymoose.domain.model.offer.BaseOffer;
import com.heymoose.domain.model.offer.Category;
import com.heymoose.domain.model.offer.CategoryGroup;
import com.heymoose.domain.model.offer.Offer;
import com.heymoose.domain.model.offer.OfferRepository;
import com.heymoose.domain.model.statistics.OfferStat;
import com.heymoose.infrastructure.hibernate.OfferRepositoryHiber;
import com.heymoose.domain.model.offer.SubOffer;
import com.heymoose.domain.model.offer.SubOfferRepository;
import com.heymoose.domain.service.Tracking;
import com.heymoose.domain.model.base.Repo;
import com.heymoose.domain.model.counter.BufferedClicks;
import com.heymoose.domain.model.counter.BufferedShows;
import com.heymoose.infrastructure.hibernate.SubOfferRepositoryHiber;
import com.heymoose.infrastructure.hibernate.UserRepositoryHiber;
import com.heymoose.domain.model.mlm.Mlm;
import com.heymoose.domain.model.mlm.MlmExecution;
import com.heymoose.domain.model.settings.Setting;
import com.heymoose.domain.model.settings.Settings;

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

    bindEntities(Offer.class, User.class, Banner.class, Withdraw.class,
        Setting.class, Site.class,
        BaseOffer.class, SubOffer.class, OfferGrant.class,
        Category.class, CategoryGroup.class,
        IpSegment.class, Account.class,
        AdminAccount.class, AccountingEntry.class, AccountingTransaction.class,
        OfferStat.class, OfferAction.class,
        AdminAccountNotConfirmed.class, Token.class, MlmExecution.class,
        KeywordPattern.class, ErrorInfo.class);
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
