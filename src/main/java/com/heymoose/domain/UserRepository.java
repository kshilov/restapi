package com.heymoose.domain;

import com.heymoose.domain.base.Repository;
import com.heymoose.domain.Role;

public interface UserRepository extends Repository<User> {
  User byEmail(String email);
  Iterable<User> list(int offset, int limit);
  Iterable<User> list(int offset, int limit, Role role);
  Iterable<User> referrals(long userId);
  long count();
  long count(Role role);
}
