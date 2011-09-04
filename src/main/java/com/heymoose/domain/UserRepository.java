package com.heymoose.domain;

public interface UserRepository extends Repository<User> {
  User byEmail(String email);
}
