package com.heymoose.domain;

import com.heymoose.domain.base.Repository;
import com.heymoose.domain.Role;

public interface UserRepository extends Repository<User> {
  User byEmail(String email);
  Iterable<User> list(int offset, int limit, Ordering ord, boolean asc);
  Iterable<User> list(int offset, int limit, Ordering ord, boolean asc, Role role);
  Iterable<User> referrals(long userId);
  long count();
  long count(Role role);
  
  public enum Ordering {
    ID,
    EMAIL,
    LAST_NAME,
    ORGANIZATION,
    REGISTER_TIME,
    CONFIRMED,
    STAT_PAYMENTS
  }
}
