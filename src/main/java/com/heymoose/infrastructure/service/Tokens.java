package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.statistics.Token;

public class Tokens {

  private final Repo repo;

  @Inject
  public Tokens(Repo repo) {
    this.repo = repo;
  }

  public Token byValue(String value) {
    return repo.byHQL(Token.class, "from Token where value = ?", value);
  }
}
