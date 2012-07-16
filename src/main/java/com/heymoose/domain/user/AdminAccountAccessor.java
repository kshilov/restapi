package com.heymoose.domain.user;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.DetachedCriteria;

@Singleton
public class AdminAccountAccessor {

  private final Repo repo;

  @Inject
  public AdminAccountAccessor(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Account getAdminAccount() {
    DetachedCriteria criteria = DetachedCriteria.forClass(AdminAccount.class);
    AdminAccount adminAccount = repo.byCriteria(criteria);
    if (adminAccount == null) {
      adminAccount = new AdminAccount();
      repo.put(adminAccount);
    }
    return adminAccount.account();
  }

  @Transactional
  public Account getAdminAccountNotConfirmed() {
    DetachedCriteria criteria = DetachedCriteria.forClass(AdminAccountNotConfirmed.class);
    AdminAccountNotConfirmed adminAccount = repo.byCriteria(criteria);
    if (adminAccount == null) {
      adminAccount = new AdminAccountNotConfirmed();
      repo.put(adminAccount);
    }
    return adminAccount.account();
  }
}
