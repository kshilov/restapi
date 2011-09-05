package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface UserRepository extends Repository<User> {
  User byEmail(String email);
}
